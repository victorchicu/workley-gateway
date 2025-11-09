package ai.workley.gateway.chat.infrastructure.embedding.mongodb;

import ai.workley.gateway.chat.domain.Embedding;
import ai.workley.gateway.chat.infrastructure.embedding.EmbeddingRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MongoEmbeddingRepositoryAdapter implements EmbeddingRepository {
    private final MongoEmbeddingRepository mongoEmbeddingRepository;

    public MongoEmbeddingRepositoryAdapter(MongoEmbeddingRepository mongoEmbeddingRepository) {
        this.mongoEmbeddingRepository = mongoEmbeddingRepository;
    }

    @Override
    public Mono<Embedding> save(Embedding embedding) {
        EmbeddingDocument entity = toEmbeddingDocument(embedding);
        return mongoEmbeddingRepository.save(entity)
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
