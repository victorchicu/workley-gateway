package ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.projection;

import ai.jobbortunity.gateway.chat.domain.model.Role;
import ai.jobbortunity.gateway.chat.domain.event.ReplyInitiated;
import ai.jobbortunity.gateway.chat.domain.event.ReplyGenerated;
import ai.jobbortunity.gateway.chat.infrastructure.config.props.OpenAiChatOptions;
import ai.jobbortunity.gateway.chat.application.error.InfrastructureErrors;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.entity.MessageModel;
import ai.jobbortunity.gateway.chat.infrastructure.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.repository.MessageReadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.ResponseFormat;
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

    private final IdGenerator messageIdGenerator;
    private final OpenAiChatModel openAiChatModel;
    private final OpenAiChatOptions openAiChatOptions;
    private final MessageReadRepository messageReadRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<ai.jobbortunity.gateway.chat.domain.model.Message<String>> chatSink;

    public GenerateReplyProjection(
            IdGenerator messageIdGenerator,
            OpenAiChatModel openAiChatModel,
            OpenAiChatOptions openAiChatOptions,
            MessageReadRepository messageReadRepository,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<ai.jobbortunity.gateway.chat.domain.model.Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.openAiChatModel = openAiChatModel;
        this.openAiChatOptions = openAiChatOptions;
        this.messageIdGenerator = messageIdGenerator;
        this.messageReadRepository = messageReadRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(ReplyInitiated e) {
        final String messageId = messageIdGenerator.generate();

        org.springframework.ai.openai.OpenAiChatOptions openAiChatOptions = org.springframework.ai.openai.OpenAiChatOptions.builder()
                .model(this.openAiChatOptions.getModel())
                .maxTokens(1000)
                .temperature(0.2)
                .responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.TEXT).build())
                .build();

        String systemPrompt = e.classificationResult().intent().getSystemPrompt();
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemPrompt), new UserMessage(e.prompt())),
                openAiChatOptions
        );

        return openAiChatModel.stream(prompt)
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
                                                new ReplyGenerated(e.actor(), e.chatId(), message.content())
                                        )
                                )
                )
                .doOnError(error -> log.error(
                        "Failed to generate reply (actor={}, chatId={}, prompt={})",
                        e.actor(), e.chatId(), e.prompt(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    private void emitChunk(ReplyInitiated e, String messageId, String chunk) {
        ai.jobbortunity.gateway.chat.domain.model.Message<String> message = ai.jobbortunity.gateway.chat.domain.model.Message.response(
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

    private Mono<ai.jobbortunity.gateway.chat.domain.model.Message<String>> saveMessage(ReplyInitiated e, String messageId, String content) {
        MessageModel<String> messageModel = MessageModel.create(
                Role.ASSISTANT, e.chatId(), e.actor(), messageId, Instant.now(), content);

        return messageReadRepository.save(messageModel)
                .map(saved -> ai.jobbortunity.gateway.chat.domain.model.Message.response(
                        saved.getId(), saved.getChatId(), e.actor(),
                        saved.getRole(), saved.getCreatedAt(), saved.getContent()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.error("Failed to save reply (actor={}, chatId={}, messageId={}, prompt={})",
                            e.actor(), e.chatId(), messageId, e.prompt(), error);
                    return Mono.empty();
                });
    }
}
