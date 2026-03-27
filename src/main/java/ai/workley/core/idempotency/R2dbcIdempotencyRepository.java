package ai.workley.core.idempotency;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface R2dbcIdempotencyRepository extends ReactiveCrudRepository<IdempotencyEntity, String> {
}
