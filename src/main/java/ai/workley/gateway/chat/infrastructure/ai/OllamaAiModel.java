package ai.workley.gateway.chat.infrastructure.ai;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.infrastructure.exceptions.AiModelUnavailableException;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Primary
@Component
public class OllamaAiModel implements AiModel {
    private final OllamaChatModel ollamaChatModel;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    public OllamaAiModel(OllamaChatModel ollamaChatModel, ReactiveCircuitBreakerFactory<?, ?> reactiveCircuitBreakerFactory) {
        this.ollamaChatModel = ollamaChatModel;
        this.reactiveCircuitBreaker = reactiveCircuitBreakerFactory.create("OllamaAiModel");
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return Flux.defer(() ->
                reactiveCircuitBreaker.run(
                        ollamaChatModel.stream(prompt)
                                .onErrorMap(this::toAiModelUnavailableException), this::fallback)
        );
    }

    private Throwable toAiModelUnavailableException(Throwable ex) {
        return ex instanceof AiModelUnavailableException
                ? ex
                : new AiModelUnavailableException(ex);
    }

    private Flux<ChatResponse> fallback(Throwable ex) {
        return Flux.error(toAiModelUnavailableException(ex));
    }
}