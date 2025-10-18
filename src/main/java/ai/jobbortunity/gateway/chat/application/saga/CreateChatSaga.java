package ai.jobbortunity.gateway.chat.application.saga;

import ai.jobbortunity.gateway.chat.application.bus.CommandBus;
import ai.jobbortunity.gateway.chat.domain.model.Message;
import ai.jobbortunity.gateway.chat.application.command.AddMessage;
import ai.jobbortunity.gateway.chat.domain.event.ChatCreated;
import ai.jobbortunity.gateway.chat.infrastructure.service.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Component
public class CreateChatSaga {
    private static final Logger log = LoggerFactory.getLogger(CreateChatSaga.class);

    private final IdGenerator randomIdGenerator;
    private final CommandBus commandBus;

    CreateChatSaga(
            IdGenerator randomIdGenerator,
            CommandBus commandBus
    ) {
        this.randomIdGenerator = randomIdGenerator;
        this.commandBus = commandBus;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ChatCreated e) {
        RetryBackoffSpec retry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.5)
                        .filter(this::isRetryable)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying adding prompt (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));
        return commandBus
                .execute(e.actor(),
                        new AddMessage(e.chatId(),
                                Message.anonymous(randomIdGenerator.generate(), e.chatId(), e.actor(), e.prompt()))
                )
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retry)
                .doOnSuccess(result ->
                        log.info("Execute add prompt command (actor={}, chatId={}, prompt={})",
                                e.actor(), e.chatId(), e.prompt()))
                .onErrorResume(error -> {
                    log.error("Failed to add prompt even after all retry attempts (actor={}, chatId={}, prompt={})",
                            e.actor(), e.chatId(), e.prompt(), error);
                    return Mono.empty();
                })
                .then();
    }

    private boolean isRetryable(Throwable throwable) {
        return true;
    }
}
