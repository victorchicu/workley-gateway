package ai.workley.gateway.chat.infrastructure.embedding.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface R2dbcEmbeddingRepository extends ReactiveCrudRepository<EmbeddingEntity, Long> {
}
