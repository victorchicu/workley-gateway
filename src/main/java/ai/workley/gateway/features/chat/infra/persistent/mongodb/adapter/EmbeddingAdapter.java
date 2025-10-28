package ai.workley.gateway.features.chat.infra.persistent.mongodb.adapter;

import ai.workley.gateway.features.chat.app.port.EmbeddingPort;
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
    public Mono<EmbeddingDocument> save(EmbeddingDocument embedding) {
        return embeddingRepository.save(embedding);
    }
}
