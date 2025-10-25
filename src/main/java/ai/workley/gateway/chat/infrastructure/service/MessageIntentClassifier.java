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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MessageIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(MessageIntentClassifier.class);

    private static final String SYSTEM_PROMPT = """
            You are an intent classifier for job/talent search platform.
            Classify the user message into one of these categories:
            
            Field descriptions:
            - intent: The classified intent (SEARCH_JOB, SEARCH_CANDIDATE, CREATE_RESUME or UNRELATED)
            - reasoning: 1-2 sentence explanation of the classification.
            - confidence: Float between 0 and 1 indicating classification confidence.
            - unrelated: What the user is actually talking about in format UPPER_CASE with underscores.
            
            Respond ONLY with valid JSON matching this structure:
            {
              "intent": "SEARCH_JOB",
              "reasoning": "explanation here",
              "confidence": 0.95,
              "unrelated": null
            }
            
            DO NOT include any text outside the JSON object. DO NOT use markdown code blocks.
            """;

    private final AiModel aiModel;
    private final ObjectMapper objectMapper;

    public MessageIntentClassifier(AiModel aiModel, ObjectMapper objectMapper) {
        this.aiModel = aiModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<ClassificationResult> classify(Message<String> message) {
        log.debug("Classifying intent for: {}", message.content());

        Prompt prompt =
                new Prompt(List.of(
                        new SystemMessage(SYSTEM_PROMPT), new UserMessage(message.content())));

        return aiModel.stream(prompt)
                .mapNotNull(response -> response.getResult().getOutput().getText())
                .filter(content -> content != null && !content.isEmpty())
                .reduce("", (accumulator, chunk) -> accumulator + chunk)
                .map(String::trim)
                .map(this::parseResponse)
                .doOnSuccess(intent -> log.info("Classified as: {}", intent))
                .onErrorResume(error -> {
                    //TODO: Save a message that could not be classified
                    log.error("Classification failed", error);
                    return Mono.just(new ClassificationResult(IntentType.UNRELATED, error.getMessage(), 0f, "CLASSIFICATION_FAILED"));
                });
    }


    private ClassificationResult parseResponse(String response) {
        try {
            return objectMapper.readValue(response, ClassificationResult.class);
        } catch (Exception e) {
            throw InfrastructureErrors.runtimeException("Failed to parse GPT response: " + response,
                    e);
        }
    }
}