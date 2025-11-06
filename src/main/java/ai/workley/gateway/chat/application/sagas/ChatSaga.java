package ai.workley.gateway.chat.application.sagas;

import ai.workley.gateway.chat.application.ports.MessagePort;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.command.SaveReply;
import ai.workley.gateway.chat.domain.events.*;
import ai.workley.gateway.chat.application.command.CommandBus;
import ai.workley.gateway.chat.infrastructure.ai.AiModel;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassification;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassifier;
import ai.workley.gateway.chat.infrastructure.intent.IntentSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatSaga {
    private static final Logger log = LoggerFactory.getLogger(ChatSaga.class);

    private final AiModel aiModel;
    private final CommandBus commandBus;
    private final MessagePort messagePort;
    private final IdGenerator randomIdGenerator;
    private final IntentSuggester intentSuggester;
    private final IntentClassifier intentClassifier;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<Message<String>> chatSink;

    public ChatSaga(
            AiModel aiModel,
            CommandBus commandBus,
            MessagePort messagePort,
            IdGenerator randomIdGenerator,
            IntentSuggester intentSuggester,
            IntentClassifier intentClassifier,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<Message<String>> chatSink
    ) {
        this.aiModel = aiModel;
        this.commandBus = commandBus;
        this.messagePort = messagePort;
        this.randomIdGenerator = randomIdGenerator;
        this.intentSuggester = intentSuggester;
        this.intentClassifier = intentClassifier;
        this.applicationEventPublisher = applicationEventPublisher;
        this.chatSink = chatSink;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ChatCreated e) {
        RetryBackoffSpec retry =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.5)
                        //.filter(this::isRetryable)
                        .maxBackoff(Duration.ofSeconds(5))
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying adding reply (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()));

        Message<String> message =
                Message.create(randomIdGenerator.generate(), e.chatId(), e.actor(), e.prompt());

        return commandBus.execute(e.actor(), new AddMessage(e.chatId(), message))
                .timeout(Duration.ofSeconds(5))
                .retryWhen(retry)
                .doOnSuccess(result ->
                        log.info("Execute add message command (actor={}, chatId={}, reply={})",
                                e.actor(), e.chatId(), e.prompt()))
                .onErrorResume(error -> {
                    log.error("Failed to add message even after all retry attempts (actor={}, chatId={}, reply={})",
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

        return commandBus.execute(e.actor(), new GenerateReply(e.chatId(), e.message()))
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
                })
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyInitiated e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return messagePort.findRecentConversation(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> {
                    return intentClassifier.classify(e.prompt())
                            .timeout(Duration.ofSeconds(60))
                            .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal -> {
                                log.warn("Retrying classify intent (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                        e.actor(), e.chatId(), e.prompt().content(),
                                        retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                            }))
                            .doOnError(err -> {
                                log.error("Intent classification failed (actor={}, chatId={}, reply={})",
                                        e.actor(), e.chatId(), e.prompt().content(), err);
                            })
                            .flatMap(classification -> {
                                log.info("Intent classified as {} with confidence {} (actor={}, chatId={}, message={})",
                                        classification.intent(), classification.confidence(),
                                        e.actor(), e.chatId(), e.prompt().content());
                                return streamReply(e, classification, history);
                            });
                })
                .then();
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyGenerated e) {
        RetryBackoffSpec retryBackoffSpec =
                Retry.backoff(5, Duration.ofMillis(500))
                        .jitter(0.50)
                        .maxBackoff(Duration.ofSeconds(5));

        return commandBus.execute(e.actor(), new SaveReply(e.chatId(), e.reply()))
                .timeout(Duration.ofSeconds(60))
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                        log.warn("Retrying saving reply (actor={}, chatId={}, reply={}) attempt #{} due to {}",
                                e.actor(), e.chatId(), e.reply().content(),
                                retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                )
                .doOnSuccess(v ->
                        log.info("Execute save reply command (actor={}, chatId={}, reply={})",
                                e.actor(), e.chatId(), e.reply().content()))
                .onErrorResume(error -> {
                    log.error("Save reply failed even after all retry attempts (actor={}, chatId={}, reply={})",
                            e.actor(), e.chatId(), e.reply().content(), error);
                    return Mono.empty();
                })
                .then();
    }

    private void emitChunkSafe(ReplyInitiated e, String id, String chunk) {
        Message<String> message = Message.create(id, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);
        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);
        if (emitResult.isFailure()) {
            log.warn("Dropped chunk (actor={}, chatId={}, reason={})", e.actor(), e.chatId(), emitResult);
        }
    }

    private Mono<Void> streamReply(ReplyInitiated e, IntentClassification classification, List<Message<String>> history) {
        final String messageId = randomIdGenerator.generate();

        Flux<String> chunks = aiModel.stream(buildPrompt(e, classification, history))
                .timeout(Duration.ofSeconds(30), Flux.empty())
                .replay()
                .autoConnect()
                .flatMapIterable(resp -> {
                    List<Generation> gens = resp != null
                            ? resp.getResults()
                            : null;
                    return gens != null ? gens : List.of();
                })
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(chunk -> chunk != null && !chunk.isBlank())
                .doOnNext(chunk -> emitChunkSafe(e, messageId, chunk))
                .doOnError(error ->
                        log.error("Stream reply failed (actor={}, chatId={})",
                                e.actor(), e.chatId(), error))
                .onErrorResume(error -> Flux.empty())
                .share();

        return chunks
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .defaultIfEmpty("")
                .doOnSuccess(reply -> {
                    applicationEventPublisher.publishEvent(
                            new ReplyGenerated(
                                    e.actor(), e.chatId(), Message.create(messageId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), reply)));
                })
                .then();
    }

    private Prompt buildPrompt(ReplyInitiated e, IntentClassification classification, List<Message<String>> history) {
        List<org.springframework.ai.chat.messages.Message> list = new ArrayList<>();
        list.add(new SystemMessage(classification.getSystemPrompt()));
        if (history.isEmpty()) {
            list.add(new UserMessage(e.prompt().content()));
        } else {
            for (Message<String> message : history) {
                switch (message.role()) {
                    case ANONYMOUS,
                         CUSTOMER -> list.add(new UserMessage(message.content()));
                    case ASSISTANT -> list.add(new AssistantMessage(message.content()));
                    default -> log.warn("Ignoring role in history: {}", message.role());
                }
            }
        }
        return new Prompt(list);
    }
}
