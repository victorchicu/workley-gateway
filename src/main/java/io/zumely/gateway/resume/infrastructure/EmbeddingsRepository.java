package io.zumely.gateway.resume.infrastructure;

import io.zumely.gateway.resume.infrastructure.data.EmbeddingObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbeddingsRepository extends ReactiveMongoRepository<EmbeddingObject, String> {
}
