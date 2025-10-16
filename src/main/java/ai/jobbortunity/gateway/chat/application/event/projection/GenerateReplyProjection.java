package ai.jobbortunity.gateway.chat.application.event.projection;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.command.Role;
import ai.jobbortunity.gateway.chat.application.event.impl.GenerateReplyEvent;
import ai.jobbortunity.gateway.chat.application.event.impl.ReplyGeneratedEvent;
import ai.jobbortunity.gateway.chat.application.intent.IntentAiChatOptions;
import ai.jobbortunity.gateway.chat.application.intent.IntentType;
import ai.jobbortunity.gateway.chat.infrastructure.exception.InfrastructureExceptions;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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
    private final ObjectMapper objectMapper;
    private final OpenAiChatModel openAiChatModel;
    private final IntentAiChatOptions intentAiChatOptions;
    private final MessageHistoryRepository messageHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Sinks.Many<Message<String>> chatSink;

    public GenerateReplyProjection(
            IdGenerator messageIdGenerator,
            ObjectMapper objectMapper,
            OpenAiChatModel openAiChatModel,
            IntentAiChatOptions intentAiChatOptions,
            MessageHistoryRepository messageHistoryRepository,
            ApplicationEventPublisher applicationEventPublisher,
            Sinks.Many<Message<String>> chatSink

    ) {
        this.chatSink = chatSink;
        this.objectMapper = objectMapper;
        this.openAiChatModel = openAiChatModel;
        this.intentAiChatOptions = intentAiChatOptions;
        this.messageIdGenerator = messageIdGenerator;
        this.messageHistoryRepository = messageHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(GenerateReplyEvent e) {
        return Mono.defer(() -> Mono.just(new StreamContext(e, messageIdGenerator.generate())))
                .flatMap(ctx -> {
                    Prompt prompt = buildPromptByIntent(e);

                    return openAiChatModel.stream(prompt)
                            .timeout(Duration.ofSeconds(60))
                            .map(this::extractText)
                            .filter(Objects::nonNull)
                            .filter(s -> !s.isEmpty())
                            .doOnNext(chunk -> emitChunk(ctx, chunk))
                            .reduce(new StringBuilder(), StringBuilder::append)
                            .map(StringBuilder::toString)
                            .filter(content -> !content.isEmpty())
                            .flatMap(content -> {
                                return saveMessage(ctx, content)
                                        .doOnNext(message ->
                                                applicationEventPublisher.publishEvent(
                                                        new ReplyGeneratedEvent(e.actor(), e.chatId(), message.content()))
                                        );
                            });
                })
                .doOnError(error -> log.error("Failed to generate reply (actor={}, chatId={}, prompt={})",
                        e.actor(), e.chatId(), e.prompt(), error))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    private Prompt buildPromptByIntent(GenerateReplyEvent event) {
        String systemPrompt =
                switch (event.intent().intentType()) {
                    case JOB_SEARCH -> applyJobSearchPrompt();
                    case CANDIDATE_SEARCH -> applyCandidateSearchPrompt();
                    case OFF_TOPIC -> applyOfftopicPrompt();
                };

        String prompt = buildUserPrompt(event);

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(intentAiChatOptions.getModel())
                .build();

        return new Prompt(List.of(new SystemMessage(systemPrompt), new UserMessage(prompt)), chatOptions);
    }

    private String applyJobSearchPrompt() {
        return """
                You are Jobbortunity's AI assistant, specialized in helping users find job opportunities.
                
                Your role is to:
                - Understand the user's job search requirements
                - Ask clarifying questions when needed (experience level, location preferences, salary expectations, etc.)
                - Provide relevant job recommendations in a conversational manner
                - Guide users through their job search journey
                
                Important guidelines:
                - Be conversational and friendly
                - Ask one question at a time to avoid overwhelming the user
                -
                
                Keep responses natural and helpful.
                """;
    }

    private String applyCandidateSearchPrompt() {
        return """
                You are Jobbortunity's AI assistant, specialized in helping employers find suitable candidates.
                
                Your role is to:
                - Understand the employer's hiring requirements
                - Ask clarifying questions about the role, required skills, and candidate preferences
                - Help refine search criteria to find the best matching candidates
                
                Important guidelines:
                - Be professional and efficient
                - Ask one question at a time
                
                Keep responses focused on finding the right talent.
                """;
    }

    private String applyOfftopicPrompt() {
        return """
                You are Jobbortunity's AI assistant. The user has asked something unrelated to job searching or candidate searching.
                
                Your role is to:
                - Politely redirect them back to Jobbortunity's core features
                - Briefly explain what you can help with
                - Be friendly but clear about your limitations
                
                Keep it brief, friendly, and redirect to what you can do.
                """;
    }

    private String buildUserPrompt(GenerateReplyEvent event) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("User message: ").append(event.prompt()).append("\n\n");

        if (event.intent() != null) {
            prompt.append("Intent Analysis:\n");
            prompt.append("- Type: ").append(event.intent().intentType()).append("\n");
            prompt.append("- Reasoning: ").append(event.intent().reasoning()).append("\n");
            prompt.append("- Confidence: ").append(event.intent().confidence()).append("\n");

            if (event.intent().intentType() == IntentType.OFF_TOPIC &&
                    event.intent().offtopic() != null && !event.intent().offtopic().isEmpty()) {
                prompt.append("- Off-topic reason: ").append(event.intent().offtopic()).append("\n");
            }
        }

        return prompt.toString();
    }

    private void emitChunk(StreamContext ctx, String chunk) {
        Message<String> message =
                Message.response(ctx.messageId(), ctx.e().chatId(), ctx.e().actor(), Role.ASSISTANT, Instant.now(), chunk);

        Sinks.EmitResult emitResult = chatSink.tryEmitNext(message);

        if (emitResult.isFailure()) {
            log.debug("Failed to emit chunk (actor={}, chatId={}, chunk={}) -> ({})",
                    ctx.e().actor(), ctx.e().chatId(), chunk, emitResult);
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

    private Mono<Message<String>> saveMessage(StreamContext ctx, String content) {
        MessageObject<String> messageObject = MessageObject.create(
                Role.ASSISTANT, ctx.e().chatId(), ctx.e().actor(), ctx.messageId(), Instant.now(), content
        );

        return messageHistoryRepository.save(messageObject)
                .map(saved -> Message.response(
                        saved.getId(), saved.getChatId(), ctx.e().actor(),
                        saved.getRole(), saved.getCreatedAt(), saved.getContent()))
                .onErrorResume(InfrastructureExceptions::isDuplicateKey, error -> {
                    log.error("Failed to save reply (actor={}, chatId={}, prompt={}, prompt={})",
                            ctx.e().actor(), ctx.e().chatId(), ctx.messageId(), ctx.e().prompt(), error);
                    return Mono.empty();
                });
    }

    private record StreamContext(GenerateReplyEvent e, String messageId) {
    }
}
