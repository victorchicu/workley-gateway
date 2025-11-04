package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.chat.app.port.MessagePort;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.infra.prompt.ClassificationResult;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.Role;
import ai.workley.gateway.features.chat.domain.event.ReplyCompleted;
import ai.workley.gateway.features.chat.domain.event.ReplyGenerated;
import ai.workley.gateway.features.shared.infra.ai.AiModel;
import ai.workley.gateway.features.chat.infra.generators.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GenerateReplyProjection {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyProjection.class);

    private final AiModel aiModel;
    private final IdGenerator messageIdGenerator;
    private final MessagePort messagePort;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<Message<String>> chatSink;

    public GenerateReplyProjection(
            AiModel aiModel,
            MessagePort messagePort,
            IdGenerator messageIdGenerator,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.aiModel = aiModel;
        this.messagePort = messagePort;
        this.messageIdGenerator = messageIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async
    @EventListener
    @Order(0)
    public void handle(ReplyGenerated e) {
        messagePort.findRecentConversation(e.chatId(), 100)
                .collectList()
                .flatMap(history -> {
                    List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();

                    ClassificationResult classification = e.classification();
                    String systemPrompt = classification.intent().getSystemPrompt(e.classification().confidence());

                    if (systemPrompt != null && !systemPrompt.isBlank()) {
                        messages.add(new SystemMessage(systemPrompt));
                        log.info("Using {} confidence system prompt for intent: {} (actor={}, chatId={})",
                                classification.confidence(), classification.intent(), e.actor(), e.chatId());
                    }

                    for (Message<String> message : history) {
                        switch (message.role()) {
                            case ANONYMOUS, CUSTOMER -> messages.add(new UserMessage(message.content()));
                            case ASSISTANT -> messages.add(new AssistantMessage(message.content()));
                        }
                    }

                    final String id = messageIdGenerator.generate();

                    return aiModel.stream(new Prompt(messages))
                            .timeout(Duration.ofSeconds(60))
                            .map(this::extractText)
                            .filter(Objects::nonNull)
                            .filter(chunk -> !chunk.isEmpty())
                            .doOnNext(chunk -> emitChunk(e, id, chunk))
                            .reduce(new StringBuilder(), StringBuilder::append)
                            .map(StringBuilder::toString)
                            .filter(reply -> !reply.isEmpty())
                            .flatMap(reply ->
                                    saveMessage(e, id, reply)
                                            .doOnNext(message ->
                                                    applicationEventPublisher.publishEvent(
                                                            new ReplyCompleted(e.actor(), e.chatId(), message))))
                            .doOnError(error ->
                                    log.error("Failed to generate reply (actor={}, chatId={}, prompt={})",
                                            e.actor(), e.chatId(), e.prompt(), error))
                            .onErrorResume(error -> Mono.empty());
                })
                .subscribe();
    }

    private void emitChunk(ReplyGenerated e, String id, String chunk) {
        Message<String> message =
                Message.create(id, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), chunk);

        log.debug("Emitting reply chunk: {}", message);

        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);
        if (emitResult.isFailure()) {
            log.warn("Failed to emit chunk (actor={}, chatId={}, chunk={}) -> ({})",
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

    private Mono<Message<String>> saveMessage(ReplyGenerated e, String id, String content) {
        Message<String> message =
                Message.create(id, e.chatId(), e.actor(), Role.ASSISTANT, Instant.now(), content);

        return messagePort.save(message)
                .map(saved ->
                        Message.create(
                                saved.id(), saved.chatId(), e.actor(), saved.role(), saved.createdAt(), saved.content()))
                .doOnSuccess(reply -> log.info("Reply saved: {}", message))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Reply already exists (actor={}, chatId={}, messageId={}, prompt={})",
                            e.actor(), e.chatId(), id, e.prompt(), error);
                    return Mono.empty();
                });
    }
}
