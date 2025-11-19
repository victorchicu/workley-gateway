package ai.workley.gateway.chat.infrastructure.intent;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.application.ports.outbound.intent.IntentClassifier;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import ai.workley.gateway.chat.domain.intent.IntentClassification;
import ai.workley.gateway.chat.infrastructure.ai.ChunkReply;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class AiIntentClassifier implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(AiIntentClassifier.class);

    private static final ChatOptions JSON_ONLY =
            OllamaOptions.builder()
                    .format("json")
                    .temperature(0.0)
                    .stop(List.of("```"))
                    .build();

    private static final String ASSISTANT_PROMPT_CLASSIFICATION = """
            Classify the user's message into exactly one intent category.
            
            Intents:
            - FIND_JOB: user is looking for work/employment
            - FIND_TALENT: user wants to hire/recruit someone
            - CREATE_RESUME: user needs help with resume/CV/profile
            - UNRELATED: anything else (greetings, off-topic, unclear)
            
            Output only valid JSON with this exact structure:
            {"intent":"INTENT_NAME","confidence":0.95}
            
            Examples:
            "I need a job" -> {"intent":"FIND_JOB","confidence":0.95}
            "looking to hire a developer" -> {"intent":"FIND_TALENT","confidence":0.92}
            "help with my CV" -> {"intent":"CREATE_RESUME","confidence":0.90}
            "hello" -> {"intent":"UNRELATED","confidence":0.98}
            "what's the weather" -> {"intent":"UNRELATED","confidence":0.99}
            """;

    private final AiModel aiModel;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public AiIntentClassifier(AiModel aiModel, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.aiModel = aiModel;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<IntentClassification> classify(Message<? extends Content> message) {
        log.info("Classifying intent for: {}", message.content());

        Prompt prompt =
                new Prompt(
                        List.of(new SystemMessage(ASSISTANT_PROMPT_CLASSIFICATION), new UserMessage(message.content().toString())),
                        JSON_ONLY
                );

        return aiModel.call(prompt)
                .timeout(Duration.ofSeconds(60))
                .filter(Objects::nonNull)
                .cast(ChunkReply.class)
                .map(ChunkReply::text)
                .filter(content -> !content.isEmpty())
                .map(this::parseText)
                .name("intent.classify")
                .tag("operation", "classification")
                .tap(Micrometer.metrics(meterRegistry));
    }

    private IntentClassification parseText(String content) {
        try {
            return objectMapper.readValue(content, IntentClassification.class);
        } catch (Exception e) {
            throw InfrastructureErrors.runtimeException("Failed to convert AI model response: " + content,
                    e);
        }
    }
}
