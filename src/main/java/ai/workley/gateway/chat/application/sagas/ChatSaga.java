package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.events.*;
import ai.workley.gateway.chat.application.command.CommandBus;
import ai.workley.gateway.chat.domain.payloads.Payload;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassifier;
import ai.workley.gateway.chat.infrastructure.intent.IntentSuggester;
import ai.workley.gateway.chat.infrastructure.intent.IntentSuggestion;
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
public class ChatSaga {
    private static final Logger log = LoggerFactory.getLogger(ChatSaga.class);

    private final CommandBus commandBus;
    private final IdGenerator randomIdGenerator;
    private final IntentSuggester intentSuggester;
    private final IntentClassifier intentClassifier;

    public ChatSaga(
            CommandBus commandBus,
            IdGenerator randomIdGenerator,
            IntentSuggester intentSuggester,
            IntentClassifier intentClassifier
    ) {
        this.commandBus = commandBus;
        this.intentSuggester = intentSuggester;
        this.intentClassifier = intentClassifier;
        this.randomIdGenerator = randomIdGenerator;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ChatCreated e) {
        RetryBackoffSpec retry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.5)
                        .filter(this::isRetryable)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying adding reply (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        return commandBus.execute(e.actor(),
                        new AddMessage(e.chatId(), Message.create(randomIdGenerator.generate(), e.chatId(), e.actor(), e.prompt())))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retry)
                .doOnSuccess(result ->
                        log.info("Execute add reply command (actor={}, chatId={}, reply={})",
                                e.actor(), e.chatId(), e.prompt()))
                .onErrorResume(error -> {
                    log.error("Failed to add reply even after all retry attempts (actor={}, chatId={}, reply={})",
                            e.actor(), e.chatId(), e.prompt(), error);
                    return Mono.empty();
                })
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(MessageAdded e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        Mono<Payload> classification =
                intentClassifier.classify(e.message())
                        .timeout(Duration.ofSeconds(60))
                        .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                            log.warn("Retrying classify intent (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                    e.actor(), e.chatId(), e.message().content(),
                                    retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                        }))
                        .doOnError(err -> {
                            log.error("Intent classification failed (actor={}, chatId={}, reply={})",
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
                                            log.warn("Retrying generating reply (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                                    e.actor(), e.chatId(), e.message().content(),
                                                    retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                                    )
                                    .doOnSuccess(v ->
                                            log.info("Execute generate reply command (actor={}, chatId={}, reply={})",
                                                    e.actor(), e.chatId(), e.message().content()))
                                    .onErrorResume(error -> {
                                        log.error("Generate reply failed even after all retry attempts (actor={}, chatId={}, reply={})",
                                                e.actor(), e.chatId(), e.message().content(), error);
                                        return Mono.empty();
                                    });
                        });

        Mono<IntentSuggestion> suggestion =
                intentSuggester.suggest(e.message())
                        .timeout(Duration.ofSeconds(60))
                        .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                            log.warn("Retrying suggest intent (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                    e.actor(), e.chatId(), e.message().content(),
                                    retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                        }))
                        .doOnSuccess(result -> {
                            log.info("Intent suggested as {} (actor={}, chatId={}, message={})",
                                    result.suggestion(), e.actor(), e.chatId(), e.message().content());
                        })
                        .doOnError(err -> {
                            log.error("Intent suggestion failed (actor={}, chatId={}, reply={})",
                                    e.actor(), e.chatId(), e.message().content(), err);
                        })
                        .onErrorResume(error -> Mono.empty());

        return Mono.whenDelayError(classification, suggestion);
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyFailed e) {
        throw new UnsupportedOperationException();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyInitiated e) {
        throw new UnsupportedOperationException();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyGenerated e) {
        throw new UnsupportedOperationException();
    }

    private boolean isRetryable(Throwable throwable) {
        return true;
    }
}
