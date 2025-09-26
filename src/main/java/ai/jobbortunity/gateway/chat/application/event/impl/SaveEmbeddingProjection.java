package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.infrastructure.exception.InfrastructureExceptions;
import ai.jobbortunity.gateway.chat.infrastructure.EmbeddingsRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.EmbeddingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@EnableConfigurationProperties(SaveEmbeddingProjection.OpenAiEmbeddingOption.class)
public class SaveEmbeddingProjection {
    private static final Logger log = LoggerFactory.getLogger(SaveEmbeddingProjection.class);

    private final EmbeddingsRepository embeddingsRepository;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiEmbeddingOption openAiEmbeddingOption;

    public SaveEmbeddingProjection(
            EmbeddingsRepository embeddingsRepository,
            OpenAiEmbeddingModel openAiEmbeddingModel,
            OpenAiEmbeddingOption openAiEmbeddingOption
    ) {
        this.embeddingsRepository = embeddingsRepository;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiEmbeddingOption = openAiEmbeddingOption;
    }

    @EventListener
    public Mono<EmbeddingObject> handle(SaveEmbeddingEvent e) {
        var document = new Document(e.reference(), e.text(), Collections.emptyMap());
        return Mono.fromCallable(() -> openAiEmbeddingModel.embed(
                        List.of(document),
                        EmbeddingOptionsBuilder.builder()
                                .withModel(openAiEmbeddingOption.getModel())
                                .withDimensions(openAiEmbeddingOption.getDimension())
                                .build(),
                        new TokenCountBatchingStrategy()
                ))
                .publishOn(Schedulers.boundedElastic())
                .map(list -> list.isEmpty() ? null : list.getFirst())
                .filter(Objects::nonNull)
                .flatMap(vector -> {
                    var embedding = new EmbeddingObject()
                            .setType(e.type())
                            .setActor(e.actor())
                            .setReference(e.reference())
                            .setModel(openAiEmbeddingOption.getModel())
                            .setDimension(openAiEmbeddingOption.getDimension())
                            .setEmbedding(vector);
                    return embeddingsRepository.save(embedding)
                            .doOnSuccess(saved ->
                                    log.info("Embedding saved (actor={}, type={}, reference={})",
                                            saved.getActor(), saved.getType(), saved.getReference()))
                            .onErrorResume(InfrastructureExceptions::isDuplicateKey, error -> {
                                log.info("Embedding already exists (actor={}, type={}, reference={})",
                                        e.actor(), e.type(), e.reference());
                                return Mono.empty();
                            });
                })
                .retryWhen(
                        Retry.backoff(3, java.time.Duration.ofMillis(200))
                                .maxBackoff(java.time.Duration.ofSeconds(2))
                                .jitter(0.25)
                                .doBeforeRetry(retrySignal -> {
                                    log.warn("Retrying embedding save (actor={}, type={}, reference={}) attempt #{} due to {}",
                                            e.actor(), e.type(), e.reference(), retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                                })
                )
                .doOnError(error ->
                        log.error("Embedding failed (actor={}, type={}, reference={})",
                                e.actor(), e.type(), e.reference(), error))
                .onErrorResume(err -> Mono.empty());
    }

    @ConfigurationProperties("spring.ai.openai.embedding.options")
    public static class OpenAiEmbeddingOption {
        private String model;
        private Integer dimension;

        public String getModel() {
            return model;
        }

        public OpenAiEmbeddingOption setModel(String model) {
            this.model = model;
            return this;
        }

        public Integer getDimension() {
            return dimension;
        }

        public OpenAiEmbeddingOption setDimension(Integer dimension) {
            this.dimension = dimension;
            return this;
        }
    }
}
