package ai.workley.gateway.chat.application.ports;

import ai.workley.gateway.chat.domain.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingPort {

    Mono<Embedding> save(Embedding embedding);
}
