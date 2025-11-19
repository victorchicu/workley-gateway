package ai.workley.gateway.chat.infrastructure.ai.model;

import ai.workley.gateway.chat.application.ports.outbound.ai.AiModel;
import ai.workley.gateway.chat.infrastructure.ai.ErrorReply;
import ai.workley.gateway.chat.infrastructure.ai.ErrorCode;
import ai.workley.gateway.chat.infrastructure.ai.ReplyEvent;
import ai.workley.gateway.chat.infrastructure.ai.ChunkReply;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

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
    public Flux<ReplyEvent> stream(Prompt prompt) {
        return reactiveCircuitBreaker.run(internalStream(prompt), this::fallback);
    }

    private ErrorReply toError(Throwable t) {
        if (t instanceof CallNotPermittedException) {
            return new ErrorReply(ErrorCode.AI_MODEL_CIRCUIT_OPEN, "Circuit breaker open for Ollama model");
        }

        if (t instanceof WebClientRequestException) {
            return new ErrorReply(ErrorCode.AI_MODEL_BACKEND_UNREACHABLE, "Ollama backend unreachable");
        }

        if (t instanceof WebClientResponseException wcre) {
            return new ErrorReply(ErrorCode.AI_MODEL_HTTP_ERROR, String.format("Ollama returned HTTP error: %s", wcre.getStatusCode()));
        }

        return new ErrorReply(
                ErrorCode.UNKNOWN,
                "An unexpected error occurred in Ollama. Please try again."
        );
    }

    private Flux<ReplyEvent> fallback(Throwable t) {
        return Flux.just(toError(t));
    }

    private Flux<ReplyEvent> internalStream(Prompt prompt) {
        return ollamaChatModel.stream(prompt)
                .timeout(Duration.ofSeconds(30))
                .flatMapIterable(chatResponse -> {
                    List<Generation> generations =
                            chatResponse != null
                                    ? chatResponse.getResults()
                                    : null;
                    return generations != null
                            ? generations
                            : List.of();
                })
                .map(Generation::getOutput)
                .mapNotNull(AbstractMessage::getText)
                .map(ChunkReply::new);
    }
}