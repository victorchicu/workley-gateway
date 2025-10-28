package ai.workley.gateway.features.chat.infra.persistent.mongodb.adapter;

import ai.workley.gateway.features.chat.app.port.EmbeddingPort;
import ai.workley.gateway.features.chat.domain.Embedding;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.EmbeddingRepository;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.EmbeddingDocument;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EmbeddingAdapter implements EmbeddingPort {
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingAdapter(EmbeddingRepository embeddingRepository) {
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
