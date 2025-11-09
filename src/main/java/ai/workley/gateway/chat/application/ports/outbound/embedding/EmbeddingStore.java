package ai.workley.gateway.chat.application.ports.outbound.embedding;

import ai.workley.gateway.chat.domain.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingStore {

    Mono<Embedding> save(Embedding embedding);
}
