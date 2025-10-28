package ai.workley.gateway.features.chat.app.port;

import ai.workley.gateway.features.chat.domain.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingPort {

    Mono<Embedding> save(Embedding embedding);
}
