package ai.workley.gateway.chat.infrastructure.persistent.mongodb.adapters;

import ai.workley.gateway.chat.application.ports.EmbeddingPort;
import ai.workley.gateway.chat.domain.Embedding;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.EmbeddingRepository;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EmbeddingDocument;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EmbeddingMongoAdapter implements EmbeddingPort {
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingMongoAdapter(EmbeddingRepository embeddingRepository) {
        this.embeddingRepository = embeddingRepository;
    }

    @Override
    public Mono<Embedding> save(Embedding embedding) {
        EmbeddingDocument entity = toEmbeddingDocument(embedding);
        return embeddingRepository.save(entity)
                .map(this::toEmbedding);
    }

    private Embedding toEmbedding(EmbeddingDocument source) {
        return new Embedding(source.getId(), source.getModel(), source.getActor(), source.getDimension(), source.getEmbedding());
    }

    private EmbeddingDocument toEmbeddingDocument(Embedding source) {
        return new EmbeddingDocument()
                .setId(source.id())
                .setModel(source.model())
                .setActor(source.actor())
                .setDimension(source.dimension())
                .setEmbedding(source.embedding());
    }
}
