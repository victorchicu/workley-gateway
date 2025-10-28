package ai.workley.gateway.features.chat.app.port;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.EmbeddingDocument;
import reactor.core.publisher.Mono;

public interface EmbeddingPort {

    Mono<EmbeddingDocument> save(EmbeddingDocument embedding);
}
