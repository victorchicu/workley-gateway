package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.command.Role;
import ai.jobbortunity.gateway.chat.application.exception.Exceptions;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GenerateReplyProjection {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyProjection.class);

    private final IdGenerator messageIdGenerator;
    private final OpenAiChatModel openAiChatModel;
    private final MessageHistoryRepository messageHistoryRepository;
    private final Sinks.Many<Message<String>> chatSink;

    public GenerateReplyProjection(
            IdGenerator messageIdGenerator,
            OpenAiChatModel openAiChatModel,
            MessageHistoryRepository messageHistoryRepository,
            Sinks.Many<Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.openAiChatModel = openAiChatModel;
        this.messageIdGenerator = messageIdGenerator;
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @EventListener
    public Mono<Void> handle(GenerateReplyEvent e) {
        Prompt prompt = Prompt.builder().content(e.prompt()).build();

        return Mono.defer(() -> Mono.just(new StreamContext(e, messageIdGenerator.generate())))
                .flatMap(ctx ->
                        openAiChatModel.stream(prompt)
                                .timeout(Duration.ofSeconds(60))
                                .map(this::extractText)
                                .filter(Objects::nonNull)
                                .filter(s -> !s.isEmpty())
                                .doOnNext(chunk -> emitChunk(ctx, chunk))
                                .reduce(new StringBuilder(), StringBuilder::append)
                                .map(StringBuilder::toString)
                                .filter(content -> !content.isEmpty())
                                .flatMap(content -> saveMessage(ctx, content))
                )
                .doOnError(error -> log.error("Failed to generate reply (actor={}, type={}, prompt={})",
                        e.actor(), e.chatId(), e.prompt(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    private void emitChunk(StreamContext ctx, String chunk) {
        Message<String> message =
                Message.response(ctx.messageId(), ctx.e().chatId(), ctx.e().actor(), Role.ASSISTANT, Instant.now(), chunk);

        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);

        if (emitResult.isFailure()) {
            log.debug("Failed to emit chunk (actor={}, type={}, messageId={}) -> ({})",
                    ctx.e.actor(), ctx.e.chatId(), ctx.messageId, emitResult);
        }
    }

    private String extractText(ChatResponse response) {
        if (response == null) return "";
        List<Generation> generations = response.getResults();
        if (generations.isEmpty()) return "";
        return generations.stream()
                .filter(Objects::nonNull)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private Mono<Message<String>> saveMessage(StreamContext ctx, String content) {
        MessageObject<String> mo = MessageObject.create(
                Role.ASSISTANT, ctx.e.chatId(), ctx.e.actor(), ctx.messageId(), Instant.now(), content);

        return messageHistoryRepository.save(mo)
                .map(saved -> Message.response(
                        saved.getId(), saved.getChatId(), ctx.e.actor(),
                        saved.getRole(), saved.getCreatedAt(), saved.getContent()))
                .onErrorResume(Exceptions::isDuplicateKey, error -> {
                    log.error("Failed to save reply (actor={}, chatId={}, prompt={}, prompt={})",
                            ctx.e.actor(), ctx.e.chatId(), ctx.messageId(), ctx.e().prompt(), error);
                    return Mono.empty();
                });
    }

    private record StreamContext(GenerateReplyEvent e, String messageId) {
    }
}