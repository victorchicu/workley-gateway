package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.CommandResult;
import ai.jobbortunity.gateway.chat.application.command.impl.GenerateReplyCommand;
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
public class IdentifyIntentWorkflow {
    private static final Logger log = LoggerFactory.getLogger(IdentifyIntentWorkflow.class);

    private final CommandDispatcher commandDispatcher;

    public IdentifyIntentWorkflow(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(IdentifyIntentEvent e) {
        var embeddingRetry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .maxBackoff(Duration.ofSeconds(5))
                        .jitter(0.50)
                        .doBeforeRetry(rs ->
                                log.warn("Retrying save embedding (actor={}, type={}, reference={}) attempt #{} due to {}",
                                        e.actor(), e.getClass(), e.message().id(), rs.totalRetries() + 1, rs.failure().toString()));

        Mono<CommandResult> saveEmbedding =
                commandDispatcher
                        .dispatch(e.actor(), new SaveEmbeddingCommand(e.getClass().getName(), e.message().id(), e.message().content()))
                        .timeout(Duration.ofSeconds(5))
                        .retryWhen(embeddingRetry)
                        .doOnSuccess(result ->
                                log.info("Dispatch save embedding command (actor={}, type={}, reference={})",
                                        e.actor(), e.getClass(), e.message().id()))
                        .onErrorResume(error -> {
                            log.error("Save embedding failed even after all retry attempts (actor={}, type={}, reference={})",
                                    e.actor(), e.getClass(), e.message().id(), error);
                            return Mono.empty();
                        });

        var replyRetry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying generating reply (actor={}, chatId={}, messageId={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.message().id(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        Mono<CommandResult> generateReply =
                commandDispatcher
                        .dispatch(e.actor(), new GenerateReplyCommand(e.chatId(), e.message()))
                        .timeout(Duration.ofSeconds(5))
                        .retryWhen(replyRetry)
                        .doOnSuccess(v ->
                                log.info("Dispatch generate reply command (actor={}, messageId={}, prompt={})",
                                        e.actor(), e.message().id(), e.message().content()))
                        .onErrorResume(error -> {
                            log.error("Generate reply failed even after all retry attempts (actor={}, messageId={}, prompt={})",
                                    e.actor(), e.message().id(), e.message().content(), error);
                            return Mono.empty();
                        });

        return saveEmbedding
                .then(generateReply)
                .then();
    }
}
