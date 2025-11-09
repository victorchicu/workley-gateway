package ai.workley.gateway.chat.application.ports.outbound;

import ai.workley.gateway.chat.domain.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingService {

    Mono<Embedding> save(Embedding embedding);
}
