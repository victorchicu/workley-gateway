package ai.workley.gateway.chat.infrastructure.embedding;

import ai.workley.gateway.chat.application.ports.outbound.embedding.EmbeddingStore;
import ai.workley.gateway.chat.domain.Embedding;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EmbeddingStoreImpl implements EmbeddingStore {
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingStoreImpl(EmbeddingRepository embeddingRepository) {
        this.embeddingRepository = embeddingRepository;
    }

    @Override
    public Mono<Embedding> save(Embedding embedding) {
        return embeddingRepository.save(embedding);
    }
}
