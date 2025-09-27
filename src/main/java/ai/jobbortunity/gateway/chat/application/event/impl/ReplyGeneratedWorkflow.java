package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.CommandResult;
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
public class ReplyGeneratedWorkflow {
    private static final Logger log = LoggerFactory.getLogger(ReplyGeneratedWorkflow.class);

    private final CommandDispatcher commandDispatcher;

    public ReplyGeneratedWorkflow(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyGeneratedEvent e) {
        var embeddingRetry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying save embedding (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.reply(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        Mono<CommandResult> saveEmbedding =
                commandDispatcher
                        .dispatch(e.actor(), new SaveEmbeddingCommand(e.reply()))
                        .timeout(Duration.ofSeconds(5))
                        .retryWhen(embeddingRetry)
                        .doOnSuccess(v ->
                                log.info("Dispatch save embedding command (actor={}, chatId={}, reply={})",
                                        e.actor(), e.chatId(), e.reply()))
                        .onErrorResume(error -> {
                            log.error("Save embedding failed even after all retry attempts (actor={}, chatId={}, reply={})",
                                    e.actor(), e.chatId(), e.reply(), error);
                            return Mono.empty();
                        });

        return saveEmbedding
                .then();
    }
}
