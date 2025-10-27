package ai.workley.gateway.features.chat.infra.persistent;

import ai.workley.gateway.features.chat.infra.readmodel.EmbeddingModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingReadRepository extends ReactiveMongoRepository<EmbeddingModel, String> {
}
