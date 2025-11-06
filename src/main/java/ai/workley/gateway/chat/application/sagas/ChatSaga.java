package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.events.ChatCreated;
import ai.workley.gateway.chat.application.command.CommandBus;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
import ai.workley.gateway.chat.domain.Message;
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
public class ChatSaga {
    private static final Logger log = LoggerFactory.getLogger(ChatSaga.class);

    private final CommandBus commandBus;
    private final IdGenerator randomIdGenerator;

    public ChatSaga(CommandBus commandBus, IdGenerator randomIdGenerator) {
        this.commandBus = commandBus;
        this.randomIdGenerator = randomIdGenerator;
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
                                log.warn("Retrying adding reply (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        return commandBus.execute(e.actor(),
                        new AddMessage(e.chatId(), Message.create(randomIdGenerator.generate(), e.chatId(), e.actor(), e.prompt())))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retry)
                .doOnSuccess(result ->
                        log.info("Execute add reply command (actor={}, chatId={}, reply={})",
                                e.actor(), e.chatId(), e.prompt()))
                .onErrorResume(error -> {
                    log.error("Failed to add reply even after all retry attempts (actor={}, chatId={}, reply={})",
                            e.actor(), e.chatId(), e.prompt(), error);
                    return Mono.empty();
                })
                .then();
    }

    private boolean isRetryable(Throwable throwable) {
        return true;
    }
}
