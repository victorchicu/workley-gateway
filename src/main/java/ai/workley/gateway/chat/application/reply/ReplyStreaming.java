package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.application.chat.ChatSession;
import ai.workley.gateway.chat.application.ports.outbound.EventBus;
import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.application.ports.outbound.intent.IntentClassifier;
import ai.workley.gateway.chat.application.ports.outbound.intent.IntentSuggester;
import ai.workley.gateway.chat.domain.IntentType;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.events.ReplyCompleted;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import ai.workley.gateway.chat.domain.intent.IntentClassification;
import ai.workley.gateway.chat.domain.intent.IntentSuggestion;
import ai.workley.gateway.chat.infrastructure.ai.ChunkReply;
import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;
import ai.workley.gateway.chat.infrastructure.ai.ErrorReply;
import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReplyStreaming {
    private static final Logger log = LoggerFactory.getLogger(ReplyStreaming.class);

    private final AiModel aiModel;
    private final EventBus eventBus;
    private final ChatSession chatSession;
    private final IntentSuggester intentSuggester;
    private final IntentClassifier intentClassifier;
    private final ConversionService conversionService;
    private final Sinks.Many<Message<? extends Content>> chatSessionSink;

    public ReplyStreaming(
            AiModel aiModel,
            EventBus eventBus,
            ChatSession chatSession,
            IntentSuggester intentSuggester,
            IntentClassifier intentClassifier,
            ConversionService conversionService,
            Sinks.Many<Message<? extends Content>> chatSessionSink
    ) {
        this.aiModel = aiModel;
        this.eventBus = eventBus;
        this.chatSession = chatSession;
        this.intentSuggester = intentSuggester;
        this.intentClassifier = intentClassifier;
        this.conversionService = conversionService;
        this.chatSessionSink = chatSessionSink;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyStarted e) {
        return chatSession.loadRecentHistory(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> streamReply(e, new IntentClassification(IntentType.UNRELATED, 1.0f), history))
                .then();
    }

    private Prompt buildPrompt(ReplyStarted e, IntentClassification classification, List<Message<? extends Content>> history) {
        List<org.springframework.ai.chat.messages.Message> list = new ArrayList<>();

        list.add(new SystemMessage(classification.getSystemPrompt()));

        for (Message<? extends Content> message : history) {
            String text = Objects.requireNonNull(
                    conversionService.convert(message.content(), String.class),
                    "Can't unwrap value content");
            switch (message.role()) {
                case ANONYMOUS,
                     CUSTOMER -> list.add(new UserMessage(text));
                case ASSISTANT -> list.add(new AssistantMessage(text));
                default -> log.warn("Ignoring role in history: {}", message.role());
            }
        }

        if (history.isEmpty() || !history.getLast().id().equals(e.message().id())) {
            String text = Objects.requireNonNull(conversionService.convert(e.message().content(), String.class), "Can't unwrap value content");
            list.add(new UserMessage(text));
        }

        return new Prompt(list);
    }

    private Content transformChunk(ReplyEvent event) {
        ReplyType replyType = ReplyType.valueOf(event.type());
        return switch (event) {
            case ChunkReply(String text)
                    when replyType == ReplyType.CHUNK -> new TextContent(text);
            case ErrorReply(ErrorCode code, String message)
                    when replyType == ReplyType.ERROR -> throw new ReplyException(code, message);
            default -> throw new UnsupportedOperationException("Unsupported event type: " + event.type());
        };
    }

    private Mono<Void> streamReply(ReplyStarted e, List<Message<? extends Content>> history) {
        Mono<IntentClassification> classifier = intentClassifier.classify(e.message())
                .timeout(Duration.ofSeconds(30))
                .doOnError(err -> {
                    log.error("Intent classification failed (actor={}, chatId={})",
                            e.actor(), e.chatId(), err);
                })
                .onErrorResume(throwable ->
                        Mono.just(
                                new IntentClassification(IntentType.UNRELATED, 1.0f)))
                .cache();

        return classifier
                .flatMap(classification -> {
                    Mono<Void> suggester = intentSuggester.suggest(e.message())
                            .timeout(Duration.ofSeconds(30))
                            .doOnError(err -> {
                                log.error("Intent suggestion failed (actor={}, chatId={})",
                                        e.actor(), e.chatId(), err);
                            })
                            .onErrorResume(throwable ->
                                    Mono.just(
                                            new IntentSuggestion("UNKNOWN", 1.0f)))
                            .doOnNext(suggestion ->
                                    log.info("Intent classified as {}({}%)/{}({}%) (actor={}, chatId={})",
                                            classification.intent(), classification.confidence(), suggestion.suggestion(), suggestion.confidence(), e.actor(), e.chatId()))
                            .then();

                    return streamReply(e, classification, history)
                            .doFinally(signalType ->
                                    suggester.subscribe(null,
                                            err ->
                                                    log.error("Intent suggester subscription failed (actor={}, chatId={})",
                                                            e.actor(), e.chatId(), err)));
                })
                .doOnError(err -> {
                    log.error("Intent classification failed (actor={}, chatId={})",
                            e.actor(), e.chatId(), err);
                });
    }

    private Mono<Void> streamReply(ReplyStarted e, IntentClassification classification, List<Message<? extends Content>> history) {
        final String replyId = UUID.randomUUID().toString();

        Flux<? extends Content> chunks = aiModel.stream(buildPrompt(e, classification, history))
                .map(this::transformChunk)
                .doOnNext(chunk -> {
                    Message<? extends Content> dummy =
                            Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);
                    emitChunkSafe(e, dummy);
                })
                .onErrorResume(ReplyException.class, exception -> {
                    log.error("Error streaming reply: {}", exception.getMessage());
                    return Flux.empty();
                });

        return chunks
                .reduce(new StringBuilder(), (contentBuilder, content) -> {
                    String text = Objects.requireNonNull(conversionService.convert(content, String.class),
                            "Can't unwrap value content");
                    return contentBuilder.append(text);
                })
                .map(StringBuilder::toString)
                .defaultIfEmpty("")
                .flatMap(fullReply -> {
                    log.info("Sending reply: {}", fullReply);
                    eventBus.publishEvent(
                            new ReplyCompleted(
                                    e.actor(), e.chatId(), Message.create(replyId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), new TextContent(fullReply))));
                    return Mono.empty();
                })
                .then();
    }

    private <T extends Content> void emitChunkSafe(ReplyStarted e, Message<T> dummy) {
        Sinks.EmitResult emitResult = chatSessionSink.tryEmitNext(dummy);
        if (emitResult.isFailure()) {
            log.warn("Dropped value (actor={}, chatId={}, reason={})", e.actor(), e.chatId(), emitResult);
        }
    }
}