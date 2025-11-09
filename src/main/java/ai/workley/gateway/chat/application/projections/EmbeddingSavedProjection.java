package ai.workley.gateway.chat.application.projections;

import ai.workley.gateway.chat.application.ports.outbound.embedding.EmbeddingStore;
import ai.workley.gateway.chat.domain.Embedding;
import ai.workley.gateway.chat.domain.events.EmbeddingSaved;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Component
@EnableConfigurationProperties(EmbeddingSavedProjection.OpenAiEmbeddingOptions.class)
public class EmbeddingSavedProjection {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingSavedProjection.class);

    private final RetryBackoffSpec retryBackoffSpec =
            Retry.backoff(5, Duration.ofMillis(500))
                    .jitter(0.5)
                    .maxBackoff(Duration.ofSeconds(5));

    private final EmbeddingStore embeddingStore;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiEmbeddingOptions openAiEmbeddingOptions;

    public EmbeddingSavedProjection(
            EmbeddingStore embeddingStore,
            OpenAiEmbeddingModel openAiEmbeddingModel,
            OpenAiEmbeddingOptions openAiEmbeddingOptions
    ) {
        this.embeddingStore = embeddingStore;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiEmbeddingOptions = openAiEmbeddingOptions;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(EmbeddingSaved e) {
        var document = new Document(e.text(), e.metadata());
        return Mono.fromCallable(() -> openAiEmbeddingModel.embed(
                        List.of(document),
                        EmbeddingOptionsBuilder.builder()
                                .withModel(openAiEmbeddingOptions.getModel())
                                .withDimensions(openAiEmbeddingOptions.getDimension())
                                .build(),
                        new TokenCountBatchingStrategy()
                ))
                .publishOn(Schedulers.boundedElastic())
                .map(list -> list.isEmpty() ? null : list.getFirst())
                .filter(Objects::nonNull)
                .flatMap(vector -> {
                    var embedding =
                            Embedding.create(openAiEmbeddingOptions.getModel(), e.actor(), openAiEmbeddingOptions.getDimension(), vector);
                    return embeddingStore.save(embedding)
                            .doOnSuccess(saved ->
                                    log.info("Embedding saved (actor={})", saved.actor())
                            )
                            .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                                log.warn("Embedding already exists (actor={})", e.actor());
                                return Mono.empty();
                            });
                })
                .retryWhen(retryBackoffSpec.doBeforeRetry(retrySignal ->
                        log.warn("Retrying embedding save (actor={}) attempt #{} due to {}",
                                e.actor(), retrySignal.totalRetries() + 1, retrySignal.failure().toString()))
                )
                .doOnError(error -> log.error("Embedding failed (actor={}})", e.actor(), error))
                .onErrorResume(err -> Mono.empty())
                .then();
    }

    @ConfigurationProperties("spring.ai.openai.embedding.options")
    public static class OpenAiEmbeddingOptions {
        private String model;
        private Integer dimension;

        public String getModel() {
            return model;
        }

        public OpenAiEmbeddingOptions setModel(String model) {
            this.model = model;
            return this;
        }

        public Integer getDimension() {
            return dimension;
        }

        public OpenAiEmbeddingOptions setDimension(Integer dimension) {
            this.dimension = dimension;
            return this;
        }
    }
}
