package ai.workley.gateway.chat.infrastructure.intent;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.application.ports.outbound.intent.IntentSuggester;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import ai.workley.gateway.chat.domain.intent.IntentSuggestion;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
public class AiIntentSuggester implements IntentSuggester {
    private static final Logger log = LoggerFactory.getLogger(AiIntentSuggester.class);

    private static final ChatOptions JSON_ONLY =
            OllamaOptions.builder()
                    .format("json")
                    .temperature(0.0)
                    .stop(List.of("```"))
                    .build();

    private static final String ASSISTANT_PROMPT = """
            Describe what the user message is about in 2-3 words.
            Use UPPER_CASE format like: GREETING, JOB_LISTING, CAREER_ADVICE, etc.
            
            Return JSON only:
            {
              "suggestion": "GREETING",
              "confidence": 0.95
            }
            
            Examples:
            "hello" -> {"suggestion":"GREETING","confidence":0.98}
            "show me software jobs" -> {"suggestion":"JOB_LISTING","confidence":0.95}
            "search React developers" -> {"suggestion":"CANDIDATE_SEARCH","confidence":0.94}
            "how to prepare for interview" -> {"suggestion":"CAREER_ADVICE","confidence":0.92}
            """;

    private final AiModel aiModel;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final ConversionService conversionService;

    public AiIntentSuggester(AiModel aiModel, ObjectMapper objectMapper, MeterRegistry meterRegistry, ConversionService conversionService) {
        this.aiModel = aiModel;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.conversionService = conversionService;
    }

    @Override
    public Mono<IntentSuggestion> suggest(Message<? extends Content> message) {
        log.info("Suggesting intent for: {}", message.content());

        String text = Objects.requireNonNull(conversionService.convert(message, String.class),
                "Can't unwrap value content");

        Prompt prompt =
                new Prompt(
                        List.of(new SystemMessage(ASSISTANT_PROMPT), new UserMessage(text)),
                        JSON_ONLY
                );

        return aiModel.stream(prompt)
                .timeout(Duration.ofSeconds(60))
//                .map(this::extractText)
                .filter(Objects::nonNull)
//                .filter(s -> !s.isEmpty())
                .reduce(new StringBuilder(), StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(content -> !content.isEmpty())
                .map(this::parseText)
                .name("intent.suggest")
                .tag("operation", "suggestion")
                .tap(Micrometer.metrics(meterRegistry));
    }

//    private String extractText(ReplyEvent streamEvent) {
//        return streamEvent.chunk();
//    }

    private IntentSuggestion parseText(String text) {
        try {
            return objectMapper.readValue(text, IntentSuggestion.class);
        } catch (Exception e) {
            throw InfrastructureErrors.runtimeException("Failed to convert AI model response: " + text,
                    e);
        }
    }
}
