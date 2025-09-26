package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.CommandResult;
import ai.jobbortunity.gateway.chat.application.command.impl.GenerateReplyCommand;
import ai.jobbortunity.gateway.chat.application.command.impl.IdentifyIntentCommand;
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
public class AddMessageWorkflow {
    private static final Logger log = LoggerFactory.getLogger(AddMessageWorkflow.class);

    private final CommandDispatcher commandDispatcher;

    public AddMessageWorkflow(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(AddMessageEvent e) {
        var intentRetry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying identify intent (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.message().content(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        Mono<CommandResult> identifyIntent =
                commandDispatcher
                        .dispatch(e.actor(), new IdentifyIntentCommand(e.chatId(), e.message()))
                        .timeout(Duration.ofSeconds(5))
                        .retryWhen(intentRetry)
                        .doOnSuccess(v ->
                                log.info("Dispatch identify intent command (actor={}, chatId={}, prompt={})",
                                        e.actor(), e.chatId(), e.message().content()))
                        .onErrorResume(error -> {
                            log.error("Identify intent failed even after all retry attempts (actor={}, chatId={}, prompt={})",
                                    e.actor(), e.chatId(), e.message().content(), error);
                            return Mono.empty();
                        });

        return identifyIntent
                .then();
    }
}