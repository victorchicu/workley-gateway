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
public class AddMessageProcessManager {
    private static final Logger log = LoggerFactory.getLogger(AddMessageProcessManager.class);

    private final CommandDispatcher commandDispatcher;

    public AddMessageProcessManager(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(AddMessageEvent e) {
        var embeddingRetry =
                Retry.backoff(3, Duration.ofMillis(200))
                        .maxBackoff(Duration.ofSeconds(2))
                        .jitter(0.25)
                        .doBeforeRetry(rs ->
                                log.warn("Retrying SaveEmbedding (chatId={}, messageId={}) attempt #{} due to {}",
                                        e.chatId(), e.message().id(), rs.totalRetries() + 1, rs.failure().toString()));

        var replyRetry =
                Retry.backoff(1, Duration.ofMillis(300))
                        .jitter(0.25)
                        .maxBackoff(Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying GenerateReply (chatId={}, messageId={}) attempt #{} due to {}",
                                        e.chatId(), e.message().id(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        Mono<CommandResult> saveEmbedding =
                commandDispatcher
                        .dispatch(e.actor(), new SaveEmbeddingCommand(e.chatId(), e.message()))
                        .timeout(Duration.ofSeconds(5))
                        .retryWhen(embeddingRetry)
                        .doOnSuccess(result ->
                                log.info("SaveEmbedding succeeded (chatId={}, messageId={})",
                                        e.chatId(), e.message().id()))
                        .onErrorResume(error -> {
                            log.error("SaveEmbedding failed after retries (chatId={}, messageId={})",
                                    e.chatId(), e.message().id(), error);
                            return Mono.empty();
                        });

        Mono<CommandResult> generateReply =
                commandDispatcher
                        .dispatch(e.actor(), new GenerateReplyCommand(e.chatId(), e.message()))
                        .timeout(Duration.ofSeconds(30))
                        .retryWhen(replyRetry)
                        .doOnSuccess(v ->
                                log.info("GenerateReply succeeded (chatId={}, messageId={})",
                                        e.chatId(), e.message().id()))
                        .onErrorResume(err -> {
                            log.error("GenerateReply failed after retries (chatId={}, messageId={})",
                                    e.chatId(), e.message().id(), err);
                            return Mono.empty();
                        });

        return saveEmbedding
                .then(generateReply)
                .then();
    }
}
