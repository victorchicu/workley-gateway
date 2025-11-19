package ai.workley.gateway.chat.infrastructure.ai;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Primary
@Component
public class OllamaAiModel implements AiModel {
    private final OllamaChatModel ollamaChatModel;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    public OllamaAiModel(OllamaChatModel ollamaChatModel, ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory) {
        this.ollamaChatModel = ollamaChatModel;
        this.reactiveCircuitBreaker = reactiveResilience4JCircuitBreakerFactory.create("ollama-ai-model");
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return reactiveCircuitBreaker.run(ollamaChatModel.stream(prompt), this::underHeavyLoad);
    }
}