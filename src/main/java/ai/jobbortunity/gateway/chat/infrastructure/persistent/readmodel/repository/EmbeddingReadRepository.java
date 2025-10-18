package ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.repository;

import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.entity.EmbeddingModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingReadRepository extends ReactiveMongoRepository<EmbeddingModel, String> {
}
