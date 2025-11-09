package ai.workley.gateway.chat.infrastructure.intent;

import ai.workley.gateway.chat.application.ports.outbound.intent.IntentSuggester;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import ai.workley.gateway.chat.domain.intent.IntentSuggestion;
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
public class IntentSuggesterImpl implements IntentSuggester {
    private static final Logger log = LoggerFactory.getLogger(IntentSuggesterImpl.class);

    private static final ChatOptions JSON_ONLY =
            OllamaOptions.builder()
                    .model("llama3.2:3b")
                    .format("json")
                    .temperature(0.2)
                    .stop(List.of("```"))
                    .build();

    private static final String ASSISTANT_PROMPT = """
            Describe what the user message is about in 2-3 words.
            Use UPPER_CASE format like: JOB_LISTING, CAREER_ADVICE, GREETING, etc.
            
            Return JSON only:
            {
              "suggestion": "JOB_LISTING",
              "confidence": 0.95
            }
            
            Examples:
            "show me software jobs" -> {"suggestion":"JOB_LISTING","confidence":0.95}
            "how to prepare for interview" -> {"suggestion":"CAREER_ADVICE","confidence":0.92}
            "search React developers" -> {"suggestion":"CANDIDATE_SEARCH","confidence":0.94}
            "hello" -> {"suggestion":"GREETING","confidence":0.98}
            """;

    private final AiModel aiModel;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public IntentSuggesterImpl(AiModel aiModel, ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.aiModel = aiModel;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<IntentSuggestion> suggest(Message<String> message) {
        log.info("Suggesting intent for: {}", message.content());

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
                .name("intent.suggest")
                .tag("operation", "suggestion")
                .tap(Micrometer.metrics(meterRegistry))
                .onErrorResume(error -> {
                    log.error("Suggestion failed", error);
                    return Mono.just(new IntentSuggestion("UNKNOWN", 0f));
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

    private IntentSuggestion parseText(String text) {
        try {
            return objectMapper.readValue(text, IntentSuggestion.class);
        } catch (Exception e) {
            throw InfrastructureErrors.runtimeException("Failed to convert AI model response: " + text, e);
        }
    }
}
