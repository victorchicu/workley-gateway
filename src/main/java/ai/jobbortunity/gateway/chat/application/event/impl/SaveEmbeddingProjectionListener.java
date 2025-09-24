package ai.jobbortunity.gateway.chat.application.event.impl;

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
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@EnableConfigurationProperties(SaveEmbeddingProjectionListener.OpenAiEmbeddingOption.class)
public class SaveEmbeddingProjectionListener {
    private static final Logger log = LoggerFactory.getLogger(SaveEmbeddingProjectionListener.class);

    private final EmbeddingsRepository embeddingsRepository;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiEmbeddingOption openAiEmbeddingOption;

    public SaveEmbeddingProjectionListener(
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
        var document = new Document(e.chatId(), e.message().content(), Collections.emptyMap());

        return Mono.fromCallable(() -> openAiEmbeddingModel.embed(
                        List.of(document),
                        EmbeddingOptionsBuilder.builder()
                                .withModel(openAiEmbeddingOption.getModel())
                                .withDimensions(openAiEmbeddingOption.getDimension())
                                .build(),
                        new TokenCountBatchingStrategy()
                ))
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .map(list -> list.isEmpty() ? null : list.getFirst())
                .filter(Objects::nonNull)
                .flatMap(vector -> {
                    var embedding = new EmbeddingObject()
                            .setModel(openAiEmbeddingOption.getModel())
                            .setDimension(openAiEmbeddingOption.getDimension())
                            .setChatId(e.message().chatId())
                            .setMessageId(e.message().id())
                            .setEmbedding(vector);

                    return embeddingsRepository.save(embedding)
                            .doOnSuccess(saved ->
                                    log.info("Embedding saved: chatId={}, messageId={}",
                                            saved.getChatId(), saved.getMessageId()))
                            .onErrorResume(this::isDuplicateKey, err -> {
                                log.info("Embedding already exists (idempotent): chatId={}, messageId={}",
                                        e.message().chatId(), e.message().id());
                                return Mono.empty();
                            });
                })
                .retryWhen(
                        Retry.backoff(3, java.time.Duration.ofMillis(200))
                                .maxBackoff(java.time.Duration.ofSeconds(2))
                                .jitter(0.25)
                                .doBeforeRetry(retrySignal -> {
                                    log.warn("Retrying embedding save (chatId={}, messageId={}) attempt #{} due to {}",
                                            e.message().chatId(), e.message().id(), retrySignal.totalRetries() + 1, retrySignal.failure());
                                })
                )
                .doOnError(error ->
                        log.error("Embedding failed: chatId={}, messageId={}",
                                e.message().chatId(), e.message().id(), error))
                .onErrorResume(err -> Mono.empty());
    }

    private boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof org.springframework.dao.DuplicateKeyException;
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
