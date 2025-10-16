package ai.jobbortunity.gateway.chat.application.event.workflow;

import ai.jobbortunity.gateway.chat.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.chat.application.command.impl.GenerateReplyCommand;
import ai.jobbortunity.gateway.chat.application.event.impl.AddMessageEvent;
import ai.jobbortunity.gateway.chat.application.service.ClassificationResult;
import ai.jobbortunity.gateway.chat.application.intent.IntentClassifier;
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
public class AddMessageWorkflow {
    private static final Logger log = LoggerFactory.getLogger(AddMessageWorkflow.class);

    private final IntentClassifier intentClassifier;
    private final CommandDispatcher commandDispatcher;

    public AddMessageWorkflow(IntentClassifier intentClassifier, CommandDispatcher commandDispatcher) {
        this.intentClassifier = intentClassifier;
        this.commandDispatcher = commandDispatcher;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(AddMessageEvent e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        Mono<ClassificationResult> classifyIntent =
                intentClassifier.classify(e.message())
                        .timeout(Duration.ofSeconds(5))
                        .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                            log.warn("Retrying classify intent (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                    e.actor(), e.chatId(), e.message().content(), retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                        }))
                        .doOnError(err -> {
                            log.error("Intent classification failed (actor={}, chatId={}, prompt={})",
                                    e.actor(), e.chatId(), e.message().content(), err);
                        });

        //TODO: Determine which command to call depending on the type of intent

        return classifyIntent
                .flatMap(intent -> {
                    return commandDispatcher
                            .dispatch(e.actor(),
                                    new GenerateReplyCommand(e.chatId(), intent, e.message()))
                            .timeout(Duration.ofSeconds(5))
                            .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                                    log.warn("Retrying generating reply (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                            e.actor(), e.chatId(), e.message().content(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                            )
                            .doOnSuccess(v ->
                                    log.info("Dispatch generate reply command (actor={}, chatId={}, prompt={})",
                                            e.actor(), e.chatId(), e.message().content()))
                            .onErrorResume(error -> {
                                log.error("Generate reply failed even after all retryBackoffSpec attempts (actor={}, chatId={}, prompt={})",
                                        e.actor(), e.chatId(), e.message().content(), error);
                                return Mono.empty();
                            });
                })
                .then();
    }
}
