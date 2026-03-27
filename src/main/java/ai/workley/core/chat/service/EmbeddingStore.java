package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Embedding;
import reactor.core.publisher.Mono;

public interface EmbeddingStore {

    Mono<Embedding> save(Embedding embedding);
}
