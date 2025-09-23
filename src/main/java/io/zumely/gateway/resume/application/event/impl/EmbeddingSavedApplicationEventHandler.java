package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.infrastructure.EmbeddingsRepository;
import io.zumely.gateway.resume.infrastructure.data.EmbeddingObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EmbeddingSavedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingSavedApplicationEventHandler.class);

    private final EmbeddingsRepository embeddingsRepository;
    private final OpenAiEmbeddingModel openAiEmbeddingModel;

    public EmbeddingSavedApplicationEventHandler(
            EmbeddingsRepository embeddingsRepository,
            OpenAiEmbeddingModel openAiEmbeddingModel,
            Embedding
    ) {
        this.embeddingsRepository = embeddingsRepository;
        this.openAiEmbeddingModel = openAiEmbeddingModel;
    }

    @EventListener
    public Mono<Void> handle(EmbeddingSavedApplicationEvent source) {
        float[] embedding = openAiEmbeddingModel.embed(new Document(source.message().content()));

        EmbeddingObject embeddingObject = new EmbeddingObject()
                .setModel("text-embedding-3-small")
                .setChatId(source.message().chatId())
                .setMessageId(source.message().id())
                .setEmbedding(embedding);

        return embeddingsRepository.save(new EmbeddingObject())
                .then();
    }
}
