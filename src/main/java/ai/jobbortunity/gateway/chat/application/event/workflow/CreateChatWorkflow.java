package ai.jobbortunity.gateway.chat.application.event.workflow;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.command.impl.AddMessageCommand;
import ai.jobbortunity.gateway.chat.application.event.impl.CreateChatEvent;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
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
public class CreateChatWorkflow {
    private static final Logger log = LoggerFactory.getLogger(CreateChatWorkflow.class);

    private final IdGenerator randomIdGenerator;
    private final CommandDispatcher commandDispatcher;

    CreateChatWorkflow(
            IdGenerator randomIdGenerator,
            CommandDispatcher commandDispatcher
    ) {
        this.randomIdGenerator = randomIdGenerator;
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(CreateChatEvent e) {
        RetryBackoffSpec retry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.5)
                        .filter(this::isRetryable)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying adding prompt (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));
        return commandDispatcher
                .dispatch(e.actor(),
                        new AddMessageCommand(e.chatId(),
                                Message.anonymous(randomIdGenerator.generate(), e.chatId(), e.actor(), e.prompt()))
                )
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retry)
                .doOnSuccess(result ->
                        log.info("Dispatch add prompt command (actor={}, chatId={}, prompt={})",
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
