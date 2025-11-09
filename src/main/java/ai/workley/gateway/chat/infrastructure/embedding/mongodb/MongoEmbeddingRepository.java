package ai.workley.gateway.chat.infrastructure.embedding.mongodb;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoEmbeddingRepository extends ReactiveMongoRepository<EmbeddingDocument, String> {
}
