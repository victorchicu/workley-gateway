package ai.workley.gateway.features.chat.app.saga;

import ai.workley.gateway.features.chat.infra.prompt.ClassificationResult;
import ai.workley.gateway.features.chat.domain.command.GenerateReplyInput;
import ai.workley.gateway.features.chat.domain.event.MessageAdded;
import ai.workley.gateway.features.chat.infra.prompt.IntentClassifier;
import ai.workley.gateway.features.chat.app.command.bus.CommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Component
public class AddMessageSaga {
    private static final Logger log = LoggerFactory.getLogger(AddMessageSaga.class);

    private final CommandBus commandBus;
    private final IntentClassifier intentClassifier;

    public AddMessageSaga(CommandBus commandBus, IntentClassifier intentClassifier) {
        this.commandBus = commandBus;
        this.intentClassifier = intentClassifier;
    }

    @Async
    @EventListener
    @Order(1)
    public void handle(MessageAdded e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        intentClassifier.classify(e.message())
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                    log.warn("Retrying classify intent (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                            e.actor(), e.chatId(), e.message().content(), retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                }))
                .doOnError(err -> {
                    log.error("Intent classification failed (actor={}, chatId={}, prompt={})",
                            e.actor(), e.chatId(), e.message().content(), err);
                })
                .flatMap(classificationResult -> {
                    log.info("Intent classified as {} with confidence {} (actor={}, chatId={}, message={})",
                            classificationResult.intent(), classificationResult.confidence(), e.actor(), e.chatId(), e.message().content());

                    return commandBus.execute(e.actor(), new GenerateReplyInput(e.chatId(), e.message(), classificationResult))
                            .timeout(Duration.ofSeconds(5))
                            .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                                    log.warn("Retrying generating reply (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                            e.actor(), e.chatId(), e.message().content(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                            )
                            .doOnSuccess(v ->
                                    log.info("Execute generate reply command (actor={}, chatId={}, prompt={})",
                                            e.actor(), e.chatId(), e.message().content()))
                            .onErrorResume(error -> {
                                log.error("Generate reply failed even after all retry attempts (actor={}, chatId={}, prompt={})",
                                        e.actor(), e.chatId(), e.message().content(), error);
                                return Mono.empty();
                            });
                })
                .subscribe();
    }
}
