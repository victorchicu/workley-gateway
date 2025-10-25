package ai.workley.gateway.chat.infrastructure.persistent.readmodel.repository;

import ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity.EmbeddingModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingReadRepository extends ReactiveMongoRepository<EmbeddingModel, String> {
}
