package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.domain.Payload;
import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassifier;
import ai.workley.gateway.chat.application.command.CommandBus;
import ai.workley.gateway.chat.infrastructure.intent.IntentSuggester;
import ai.workley.gateway.chat.infrastructure.intent.IntentSuggestion;
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
    private final IntentSuggester intentSuggester;
    private final IntentClassifier intentClassifier;

    public AddMessageSaga(CommandBus commandBus, IntentSuggester intentSuggester, IntentClassifier intentClassifier) {
        this.commandBus = commandBus;
        this.intentSuggester = intentSuggester;
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

        Mono<Payload> classification =
                intentClassifier.classify(e.message())
                        .timeout(Duration.ofSeconds(60))
                        .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                            log.warn("Retrying classify intent (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                    e.actor(), e.chatId(), e.message().content(),
                                    retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                        }))
                        .doOnError(err -> {
                            log.error("Intent classification failed (actor={}, chatId={}, prompt={})",
                                    e.actor(), e.chatId(), e.message().content(), err);
                        })
                        .flatMap(result -> {
                            log.info("Intent classified as {} with confidence {} (actor={}, chatId={}, message={})",
                                    result.intent(), result.confidence(),
                                    e.actor(), e.chatId(), e.message().content());

                            return commandBus.execute(e.actor(),
                                            new GenerateReply(e.chatId(), e.message(), result))
                                    .timeout(Duration.ofSeconds(60))
                                    .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                                            log.warn("Retrying generating reply (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                                    e.actor(), e.chatId(), e.message().content(),
                                                    retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                                    )
                                    .doOnSuccess(v ->
                                            log.info("Execute generate reply command (actor={}, chatId={}, prompt={})",
                                                    e.actor(), e.chatId(), e.message().content()))
                                    .onErrorResume(error -> {
                                        log.error("Generate reply failed even after all retry attempts (actor={}, chatId={}, prompt={})",
                                                e.actor(), e.chatId(), e.message().content(), error);
                                        return Mono.empty();
                                    });
                        });

        Mono<IntentSuggestion> suggestion =
                intentSuggester.suggest(e.message())
                        .timeout(Duration.ofSeconds(60))
                        .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                            log.warn("Retrying suggest intent (actor={}, chatId={}, prompt={}) attempt #{} due to {}",
                                    e.actor(), e.chatId(), e.message().content(),
                                    retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                        }))
                        .doOnSuccess(result -> {
                            log.info("Intent suggested as {} (actor={}, chatId={}, message={})",
                                    result.suggestion(), e.actor(), e.chatId(), e.message().content());
                        })
                        .doOnError(err -> {
                            log.error("Intent suggestion failed (actor={}, chatId={}, prompt={})",
                                    e.actor(), e.chatId(), e.message().content(), err);
                        })
                        .onErrorResume(error -> Mono.empty());

        Mono.whenDelayError(classification, suggestion).subscribe();
    }
}
