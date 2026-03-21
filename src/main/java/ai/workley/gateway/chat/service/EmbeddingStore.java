package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingStore {

    Mono<Embedding> save(Embedding embedding);
}
