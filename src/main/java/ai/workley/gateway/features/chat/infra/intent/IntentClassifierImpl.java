package ai.workley.gateway.features.chat.infra.intent;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.infra.ai.AiModel;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.IntentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IntentClassifierImpl implements IntentClassifier {
    private static final Logger log = LoggerFactory.getLogger(IntentClassifierImpl.class);

    private static final ChatOptions JSON_ONLY =
            OllamaOptions.builder()
                    .format("json")
                    .temperature(0.0)
                    .stop(List.of("```"))
                    .build();

    private static final String ASSISTANT_PROMPT = """
             Classify user message into one intent:
             - FIND_JOB: user wants to find a job
             - FIND_TALENT: user wants to hire someone
             - CREATE_RESUME: user wants to create/edit resume
             - UNRELATED: anything else
            
             Return JSON only:
             {
               "intent": "FIND_JOB",
               "confidence": 0.95
             }
            
             Examples:
             "I need a job" -> {"intent":"FIND_JOB","confidence":0.95}
             "hire developer" -> {"intent":"FIND_TALENT","confidence":0.92}
             "help with CV" -> {"intent":"CREATE_RESUME","confidence":0.90}
             "hello" -> {"intent":"UNRELATED","confidence":0.98}
            """;

    private final AiModel aiModel;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public IntentClassifierImpl(AiModel aiModel, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.aiModel = aiModel;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<IntentClassification> classify(Message<String> message) {
        log.info("Classifying intent for: {}", message.content());

        Prompt prompt =
                new Prompt(
                        List.of(new SystemMessage(ASSISTANT_PROMPT), new UserMessage(message.content())),
                        JSON_ONLY
                );

        return aiModel.stream(prompt)
                .timeout(Duration.ofSeconds(60))
                .map(this::extractText)
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(content -> !content.isEmpty())
                .map(this::parseText)
                .name("intent.classify")
                .tag("operation", "classification")
                .tap(Micrometer.metrics(meterRegistry))
                .onErrorResume(error -> {
                    log.error("Classification failed", error);
                    return Mono.just(new IntentClassification(IntentType.UNRELATED, 0f));
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

    private IntentClassification parseText(String text) {
        try {
            return objectMapper.readValue(text, IntentClassification.class);
        } catch (Exception e) {
            throw InfrastructureErrors.runtimeException("Failed to convert AI model response: " + text,
                    e);
        }
    }
}
