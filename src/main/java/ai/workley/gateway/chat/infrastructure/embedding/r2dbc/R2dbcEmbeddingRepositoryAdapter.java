package ai.workley.gateway.chat.infrastructure.embedding.r2dbc;

import ai.workley.gateway.chat.application.ports.outbound.embedding.EmbeddingStore;
import ai.workley.gateway.chat.domain.Embedding;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class R2dbcEmbeddingRepositoryAdapter implements EmbeddingStore {
    private final R2dbcEmbeddingRepository repository;

    public R2dbcEmbeddingRepositoryAdapter(R2dbcEmbeddingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Embedding> save(Embedding embedding) {
        EmbeddingEntity entity = toEntity(embedding);
        return repository.save(entity)
                .map(this::toEmbedding);
    }

    private Embedding toEmbedding(EmbeddingEntity source) {
        return new Embedding(
                source.getId() != null ? source.getId().toString() : null,
                source.getModel(),
                source.getActor(),
                source.getDimension(),
                source.getEmbedding()
        );
    }

    private EmbeddingEntity toEntity(Embedding source) {
        return new EmbeddingEntity()
                .setModel(source.model())
                .setActor(source.actor())
                .setDimension(source.dimension())
                .setEmbedding(source.embedding());
    }
}
