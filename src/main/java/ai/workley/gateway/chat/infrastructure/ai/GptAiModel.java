package ai.workley.gateway.chat.infrastructure.ai;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class GptAiModel implements AiModel {
    private final OpenAiChatModel openAiChatModel;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    public GptAiModel(OpenAiChatModel openAiChatModel, ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory) {
        this.openAiChatModel = openAiChatModel;
        this.reactiveCircuitBreaker = reactiveResilience4JCircuitBreakerFactory.create("gpt-ai-model");
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return reactiveCircuitBreaker.run(openAiChatModel.stream(prompt), this::underHeavyLoad);
    }
}
