package ai.jobbortunity.gateway.chat.application.saga;

import ai.jobbortunity.gateway.chat.application.bus.CommandBus;
import ai.jobbortunity.gateway.chat.application.command.GenerateReply;
import ai.jobbortunity.gateway.chat.domain.event.MessageAdded;
import ai.jobbortunity.gateway.chat.application.result.ClassificationResult;
import ai.jobbortunity.gateway.chat.domain.model.MessageClassifier;
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
public class AddMessageSaga {
    private static final Logger log = LoggerFactory.getLogger(AddMessageSaga.class);

    private final CommandBus commandBus;
    private final MessageClassifier messageClassifier;

    public AddMessageSaga(CommandBus commandBus, MessageClassifier messageClassifier) {
        this.commandBus = commandBus;
        this.messageClassifier = messageClassifier;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(MessageAdded e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        Mono<ClassificationResult> classificationResult =
                messageClassifier.classify(e.message())
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

        return classificationResult
                .flatMap(result -> {
                    return commandBus
                            .execute(e.actor(), new GenerateReply(e.chatId(), e.message(), result))
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
                .then();
    }
}