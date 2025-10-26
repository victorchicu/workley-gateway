package ai.workley.gateway.chat.infrastructure.service;

import ai.workley.gateway.chat.domain.model.Message;
import ai.workley.gateway.chat.application.result.ClassificationResult;
import ai.workley.gateway.chat.infrastructure.ai.AiModel;
import ai.workley.gateway.chat.domain.model.IntentClassifier;
import ai.workley.gateway.chat.domain.model.IntentType;
import ai.workley.gateway.chat.infrastructure.error.InfrastructureErrors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(MessageIntentClassifier.class);

    private static final String ASSISTANT_PROMPT = """
            You are an intent classifier for a job and talent search platform.
            
            Classify the user message into one of the following intents:
            - FIND_JOB — the user is looking for a job or employment opportunities.
            - FIND_TALENT — the user is looking for candidates, employees, or collaborators.
            - BUILD_PROFILE — the user wants to create, edit, or improve a profile.
            - UNRELATED — the message is not related to the platform’s functions.
            
            Output fields:
            - intent: One of the enum values (FIND_JOB, FIND_TALENT, BUILD_PROFILE, UNRELATED)
            - confidence: A float between 0 and 1 representing confidence.
            
            Respond ONLY with valid JSON in this exact format (no markdown, no extra text):
            {
              "intent": "FIND_JOB",
              "confidence": 0.93,
            }
            """;

    private final AiModel aiModel;
    private final ObjectMapper objectMapper;

    public MessageIntentClassifier(AiModel aiModel, ObjectMapper objectMapper) {
        this.aiModel = aiModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ClassificationResult> classify(Message<String> message) {
        log.info("Classifying intent for: {}", message.content());

        Prompt prompt =
                new Prompt(
                        new SystemMessage(ASSISTANT_PROMPT),
                        new UserMessage(message.content()));

        return aiModel.stream(prompt)
                .timeout(Duration.ofSeconds(60))
                .map(this::extractText)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(content -> !content.isEmpty())
                .map(this::parseText)
                .doOnSuccess(classification -> log.info("Classified as: {}", classification))
                .onErrorResume(error -> {
                    //TODO: Save a message that could not be classified
                    log.error("Classification failed", error);
                    return Mono.just(new ClassificationResult(IntentType.UNRELATED, 0f));
                });
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

    private ClassificationResult parseText(String text) {
        try {
            return objectMapper.readValue(text, ClassificationResult.class);
        } catch (Exception e) {
            throw InfrastructureErrors.runtimeException("Failed to parse GPT response: " + text,
                    e);
        }
    }
}