package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.infrastructure.EmbeddingsRepository;
import ai.jobbortunity.gateway.resume.infrastructure.data.EmbeddingObject;
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

import java.util.Collections;
import java.util.List;

@Component
@EnableConfigurationProperties(EmbeddingSavedApplicationEventHandler.OpenAiEmbeddingOption.class)
public class EmbeddingSavedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingSavedApplicationEventHandler.class);

    private final EmbeddingsRepository embeddingsRepository;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;
    private final OpenAiEmbeddingOption openAiEmbeddingOption;

    public EmbeddingSavedApplicationEventHandler(
            EmbeddingsRepository embeddingsRepository,
            OpenAiEmbeddingModel openAiEmbeddingModel,
            OpenAiEmbeddingOption openAiEmbeddingOption
    ) {
        this.embeddingsRepository = embeddingsRepository;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
        this.openAiEmbeddingOption = openAiEmbeddingOption;
    }

    @EventListener
    public Mono<EmbeddingObject> handle(EmbeddingSavedApplicationEvent source) {
        List<float[]> embeddings = openAiEmbeddingModel.embed(
                List.of(new Document(source.message().id(), source.message().content(), Collections.emptyMap())),
                EmbeddingOptionsBuilder.builder()
                        .withModel(openAiEmbeddingOption.getModel())
                        .withDimensions(openAiEmbeddingOption.getDimension())
                        .build(),
                new TokenCountBatchingStrategy()
        );

        EmbeddingObject embeddingObject = new EmbeddingObject()
                .setModel(openAiEmbeddingOption.getModel())
                .setDimension(openAiEmbeddingOption.getDimension())
                .setChatId(source.message().chatId())
                .setMessageId(source.message().id())
                .setEmbedding(embeddings.getFirst());

        return embeddingsRepository.save(embeddingObject);
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
