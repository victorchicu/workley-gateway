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
                        log.warn("Retrying SaveEmbedding (author={}, chatId={}) attempt #{} due to {}",
                                e.actor().getName(), e.chatId(), retrySignal.totalRetries() + 1, retrySignal.failure().toString())
                );
        return commandDispatcher
                .dispatch(e.actor(), new SaveEmbeddingCommand(e.chatId(), e.prompt()))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(embeddingRetry)
                .doOnSuccess(v -> log.info("SaveEmbedding succeeded (author={}, chatId={})", e.actor().getName(), e.chatId()))
                .onErrorResume(err -> {
                    log.error("SaveEmbedding failed after retries (chatId={})", e.chatId(), err);
                    return Mono.empty();
                })
                .then();
    }
}
