package ai.jobbortunity.gateway.chat.infrastructure.ai;

import ai.jobbortunity.gateway.chat.domain.model.Message;
import ai.jobbortunity.gateway.chat.application.result.ClassificationResult;
import ai.jobbortunity.gateway.chat.infrastructure.config.props.ExtendedOpenAiChatOptions;
import ai.jobbortunity.gateway.chat.domain.model.MessageClassifier;
import ai.jobbortunity.gateway.chat.domain.model.IntentType;
import ai.jobbortunity.gateway.chat.infrastructure.error.InfrastructureErrors;
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
public class OpenAiMessageClassifier implements MessageClassifier {
    private static final Logger log = LoggerFactory.getLogger(OpenAiMessageClassifier.class);

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
            You are an intent classifier for job/talent search platform.
            Classify the user message into one of these categories:
            
            Field descriptions:
            - intent: The classified intent (SEARCH_JOB, SEARCH_CANDIDATE, CREATE_RESUME or UNRELATED)
            - reasoning: 1-2 sentence explanation of the classification.
            - confidence: Float between 0 and 1 indicating classification confidence.
            - unrelated: What the user is actually talking about in format UPPER_CASE with underscores.
            """;

    private final ObjectMapper objectMapper;
    private final OpenAiChatModel openAiChatModel;
    private final ExtendedOpenAiChatOptions openAiChatOptions;

    public OpenAiMessageClassifier(ObjectMapper objectMapper, OpenAiChatModel openAiChatModel, ExtendedOpenAiChatOptions openAiChatOptions) {
        this.objectMapper = objectMapper;
        this.openAiChatModel = openAiChatModel;
        this.openAiChatOptions = openAiChatOptions;
    }

    @Override
    public Mono<ClassificationResult> classify(Message<String> message) {
        log.debug("Classifying intent for: {}", message.content());

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(this.openAiChatOptions.getModel())
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
                        new SystemMessage(SYSTEM_PROMPT), new UserMessage(message.content())), openAiChatOptions);

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