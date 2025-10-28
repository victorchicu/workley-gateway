package ai.workley.gateway.features.chat.infra.persistent.mongodb;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.EmbeddingDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingRepository extends ReactiveMongoRepository<EmbeddingDocument, String> {
}
