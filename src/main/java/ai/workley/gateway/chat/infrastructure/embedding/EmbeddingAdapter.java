package ai.workley.gateway.chat.infrastructure.embedding;

import ai.workley.gateway.chat.application.ports.outbound.EmbeddingService;
import ai.workley.gateway.chat.domain.Embedding;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EmbeddingAdapter implements EmbeddingService {
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
