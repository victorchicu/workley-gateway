package ai.jobbortunity.gateway.chat.infrastructure;

import ai.jobbortunity.gateway.chat.infrastructure.data.EmbeddingObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingsRepository extends ReactiveMongoRepository<EmbeddingObject, String> {
}
