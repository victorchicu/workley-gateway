package ai.workley.gateway.chat.infrastructure.embedding;

import ai.workley.gateway.chat.domain.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingRepository {

    Mono<Embedding> save(Embedding embedding);
}
