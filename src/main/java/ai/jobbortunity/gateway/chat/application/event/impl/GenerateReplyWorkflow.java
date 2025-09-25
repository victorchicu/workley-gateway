package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.impl.SaveEmbeddingCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class GenerateReplyWorkflow {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyWorkflow.class);

    private final CommandDispatcher commandDispatcher;

    public GenerateReplyWorkflow(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(GenerateReplyEvent e) {
        var embeddingRetry = Retry.backoff(3, Duration.ofMillis(200))
                .maxBackoff(Duration.ofSeconds(2))
                .jitter(0.25)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying save embedding (actor={}, type={}, reference={}) attempt #{} due to {}",
                                e.actor().getName(), e.getClass(), e.chatId(), retrySignal.totalRetries() + 1, retrySignal.failure().toString())
                );
        return commandDispatcher
                .dispatch(e.actor(), new SaveEmbeddingCommand(e.getClass().getName(), e.chatId(), e.prompt().content()))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(embeddingRetry)
                .doOnSuccess(result ->
                        log.info("Dispatch save embedding command (author={}, type={}, reference={})",
                                e.actor().getName(), e.getClass(), e.chatId()))
                .onErrorResume(error -> {
                    log.error("Save embedding failed even after all retry attempts (actor={}, type={}, reference={})",
                            e.actor().getName(), e.getClass(), e.chatId(), error);
                    return Mono.empty();
                })
                .then();
    }
}