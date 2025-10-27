package ai.workley.gateway.features.chat.infra.projection;

import ai.workley.gateway.features.chat.application.*;
import ai.workley.gateway.features.chat.domain.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.Role;
import ai.workley.gateway.features.chat.domain.event.ReplyCompleted;
import ai.workley.gateway.features.chat.domain.event.ReplyGenerated;
import ai.workley.gateway.features.shared.infra.ai.AiModel;
import ai.workley.gateway.features.chat.infra.component.IdGenerator;
import ai.workley.gateway.features.chat.infra.persistent.MessageReadRepository;
import ai.workley.gateway.features.chat.infra.readmodel.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
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

    private final AiModel aiModel;
    private final IdGenerator messageIdGenerator;
    private final MessageReadRepository messageReadRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<Message<String>> chatSink;

    public GenerateReplyProjection(
            AiModel aiModel,
            IdGenerator messageIdGenerator,
            MessageReadRepository messageReadRepository,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.aiModel = aiModel;
        this.messageIdGenerator = messageIdGenerator;
        this.messageReadRepository = messageReadRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(ReplyGenerated e) {
        Prompt prompt =
                new Prompt(List.of(
                        new UserMessage(e.prompt())));

        final String messageId = messageIdGenerator.generate();

        return aiModel.stream(prompt)
                .timeout(Duration.ofSeconds(60))
                .map(this::extractText)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .doOnNext(chunk -> emitChunk(e, messageId, chunk))   // ← no context object
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(content -> !content.isEmpty())
                .flatMap(content ->
                        saveMessage(e, messageId, content)
                                .doOnNext(message ->
                                        applicationEventPublisher.publishEvent(
                                                new ReplyCompleted(e.actor(), e.chatId(), message.content())
                                        )
                                )
                )
                .doOnError(error -> log.error(
                        "Failed to generate reply (actor={}, chatId={}, prompt={})",
                        e.actor(), e.chatId(), e.prompt(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    private void emitChunk(ReplyGenerated e, String messageId, String chunk) {
        Message<String> message = Message.response(
                messageId, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);

        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);
        if (emitResult.isFailure()) {
            log.debug("Failed to emit chunk (actor={}, chatId={}, chunk={}) -> ({})",
                    e.actor(), e.chatId(), chunk, emitResult);
        }
    }

    private String extractText(ChatResponse response) {
        if (response == null) return "";
        List<Generation> generations = response.getResults();
        if (generations == null || generations.isEmpty()) return "";
        return generations.stream()
                .filter(Objects::nonNull)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    private Mono<Message<String>> saveMessage(ReplyGenerated e, String messageId, String content) {
        MessageModel<String> messageModel = MessageModel.create(
                Role.ASSISTANT, e.chatId(), e.actor(), messageId, Instant.now(), content);

        return messageReadRepository.save(messageModel)
                .map(saved -> Message.response(
                        saved.getId(), saved.getChatId(), e.actor(),
                        saved.getRole(), saved.getCreatedAt(), saved.getContent()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.error("Failed to save reply (actor={}, chatId={}, messageId={}, prompt={})",
                            e.actor(), e.chatId(), messageId, e.prompt(), error);
                    return Mono.empty();
                });
    }
}
