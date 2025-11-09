package ai.workley.gateway.chat.infrastructure.embedding;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingRepository extends ReactiveMongoRepository<EmbeddingDocument, String> {
}
