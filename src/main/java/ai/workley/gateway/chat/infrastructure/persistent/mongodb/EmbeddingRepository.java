package ai.workley.gateway.chat.infrastructure.persistent.mongodb;

import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EmbeddingDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingRepository extends ReactiveMongoRepository<EmbeddingDocument, String> {
}
