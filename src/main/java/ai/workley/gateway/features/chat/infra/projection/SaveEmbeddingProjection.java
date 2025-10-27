package ai.workley.gateway.features.chat.infra.projection;

import ai.workley.gateway.features.chat.infra.readmodel.EmbeddingModel;
import ai.workley.gateway.features.chat.infra.persistent.EmbeddingReadRepository;
import ai.workley.gateway.features.chat.domain.event.EmbeddingSaved;
import ai.workley.gateway.features.chat.domain.error.InfrastructureErrors;
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

import java.util.List;
import java.util.Objects;

@Component
@EnableConfigurationProperties(SaveEmbeddingProjection.OpenAiEmbeddingOptions.class)
public class SaveEmbeddingProjection {
    private static final Logger log = LoggerFactory.getLogger(SaveEmbeddingProjection.class);

    private final EmbeddingReadRepository embeddingReadRepository;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiEmbeddingOptions openAiEmbeddingOptions;

    public SaveEmbeddingProjection(
            EmbeddingReadRepository embeddingReadRepository,
            OpenAiEmbeddingModel openAiEmbeddingModel,
            OpenAiEmbeddingOptions openAiEmbeddingOptions
    ) {
        this.embeddingReadRepository = embeddingReadRepository;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiEmbeddingOptions = openAiEmbeddingOptions;
    }

    @EventListener
    @Order(0)
    public Mono<EmbeddingModel> handle(EmbeddingSaved e) {
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
                    var embedding = new EmbeddingModel()
                            .setActor(e.actor())
                            .setModel(openAiEmbeddingOptions.getModel())
                            .setDimension(openAiEmbeddingOptions.getDimension())
                            .setEmbedding(vector);
                    return embeddingReadRepository.save(embedding)
                            .doOnSuccess(saved ->
                                    log.info("Embedding saved (actor={})", saved.getActor())
                            )
                            .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                                log.info("Embedding already exists (actor={})", e.actor());
                                return Mono.empty();
                            });
                })
                .retryWhen(
                        Retry.backoff(3, java.time.Duration.ofMillis(200))
                                .maxBackoff(java.time.Duration.ofSeconds(2))
                                .jitter(0.25)
                                .doBeforeRetry(retrySignal -> {
                                    log.warn("Retrying embedding save (actor={}) attempt #{} due to {}",
                                            e.actor(), retrySignal.totalRetries() + 1, retrySignal.failure().toString());
                                })
                )
                .doOnError(error -> log.error("Embedding failed (actor={}})", e.actor(), error))
                .onErrorResume(err -> Mono.empty());
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
