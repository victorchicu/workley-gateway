package ai.workley.gateway.chat.application.reply;

import ai.workley.gateway.chat.application.chat.ChatSession;
import ai.workley.gateway.chat.application.ports.outbound.EventBus;
import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.events.ReplyCompleted;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import ai.workley.gateway.chat.infrastructure.ai.ChunkReply;
import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;
import ai.workley.gateway.chat.infrastructure.ai.ErrorReply;
import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReplyStreaming {
    private static final Logger log = LoggerFactory.getLogger(ReplyStreaming.class);

    private final AiModel aiModel;
    private final EventBus eventBus;
    private final ChatSession chatSession;
    private final PromptBuilder promptBuilder;
    private final Sinks.Many<Message<? extends Content>> chatSessionSink;

    public ReplyStreaming(
            AiModel aiModel,
            EventBus eventBus,
            ChatSession chatSession,
            PromptBuilder promptBuilder,
            Sinks.Many<Message<? extends Content>> chatSessionSink
    ) {
        this.aiModel = aiModel;
        this.eventBus = eventBus;
        this.chatSession = chatSession;
        this.promptBuilder = promptBuilder;
        this.chatSessionSink = chatSessionSink;
    }

    @EventListener
    @Order(1)
    public Mono<Void> on(ReplyStarted e) {
        return chatSession.loadRecentHistory(e.chatId(), 100)
                .collectList()
                .flatMapMany(history -> streamReply(e, history))
                .then();
    }

    private TextContent transformChunk(ReplyEvent event) {
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
        final String replyId = UUID.randomUUID().toString();

        Prompt prompt = promptBuilder.build(e.message(), history);

        Flux<TextContent> chunks = aiModel.stream(prompt)
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
                .reduce(new StringBuilder(), (contentBuilder, content) -> contentBuilder.append(content.value()))
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