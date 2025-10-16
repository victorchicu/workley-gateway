package ai.jobbortunity.gateway.chat.application.intent.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.application.intent.IntentAiChatOptions;
import ai.jobbortunity.gateway.chat.application.intent.IntentClassifier;
import ai.jobbortunity.gateway.chat.application.intent.IntentType;
import ai.jobbortunity.gateway.chat.application.service.ClassificationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OpenAiIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(OpenAiIntentClassifier.class);

    public static final ResponseFormat.JsonSchema INTENT_SCHEMA = ResponseFormat.JsonSchema.builder()
            .name("classificationResult")
            .schema("""
                    {
                      "type": "object",
                      "properties": {
                        "intent":     { "type": "string", "enum": ["SEARCH_JOB","SEARCH_CANDIDATE","CREATE_RESUME","UNRELATED"] },
                        "reasoning":  { "type": "string" },
                        "confidence": { "type": "number", "minimum": 0, "maximum": 1 },
                        "unrelated":   { "type": "string" }
                      },
                      "required": ["intent","reasoning","confidence","unrelated"],
                      "additionalProperties": false
                    }
                    """)
            .strict(true)
            .build();

    private static final String SYSTEM_PROMPT = """
            You are an classificationResult classifier for Jobbortunity job search platform.
            Classify the user message into one of these categories:
            
            - JOB_SEARCH: User is looking for a job
            - SEARCH_CANDIDATE: User is looking to hire someone
            - CREATE_RESUME: User wants to create a resume
            - UNRELATED: Not related to jobs or hiring
            
            Field descriptions:
            - intent: The classified intent (SEARCH_JOB, SEARCH_CANDIDATE, CREATE_RESUME or UNRELATED)
            - reasoning: 1-2 sentence explanation of the classification.
            - confidence: Float between 0 and 1 indicating classification confidence.
            - unrelated: What the user is actually talking about in format UPPER_CASE with underscores.
            """;

    private final ObjectMapper objectMapper;
    private final OpenAiChatModel openAiChatModel;
    private final IntentAiChatOptions intentAiChatOptions;

    public OpenAiIntentClassifier(ObjectMapper objectMapper, OpenAiChatModel openAiChatModel, IntentAiChatOptions intentAiChatOptions) {
        this.objectMapper = objectMapper;
        this.openAiChatModel = openAiChatModel;
        this.intentAiChatOptions = intentAiChatOptions;
    }

    @Override
    public Mono<ClassificationResult> classify(Message<String> message) {
        log.debug("Classifying classificationResult for: {}", message.content());

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().model(intentAiChatOptions.getModel())
                .maxTokens(500)
                .temperature(0.2)
                .responseFormat(
                        ResponseFormat.builder()
                                .type(ResponseFormat.Type.JSON_SCHEMA)
                                .jsonSchema(INTENT_SCHEMA)
                                .build()
                )
                .build();

        Prompt prompt =
                new Prompt(List.of(
                        new SystemMessage(SYSTEM_PROMPT), new UserMessage(message.content())), chatOptions);

        return openAiChatModel.stream(prompt)
                .mapNotNull(response -> {
                    return response.getResult().getOutput().getText();
                })
                .filter(content -> {
                    return content != null && !content.isEmpty();
                })
                .reduce("", (accumulator, chunk) -> {
                    return accumulator + chunk;
                })
                .map(String::trim)
                .map(this::parseResponse)
                .doOnSuccess(intent -> log.info("Classified as: {}", intent))
                .onErrorResume(error -> {
                    log.error("Classification failed", error);
                    return Mono.just(new ClassificationResult(IntentType.UNRELATED, error.getMessage(), 0f, "CLASSIFICATION_FAILED"));
                });
    }


    private ClassificationResult parseResponse(String response) {
        try {
            return objectMapper.readValue(response, ClassificationResult.class);
        } catch (Exception e) {
            throw new ApplicationException("Failed to parse GPT response", e);
        }
    }
}