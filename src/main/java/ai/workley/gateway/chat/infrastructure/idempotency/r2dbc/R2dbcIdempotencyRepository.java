package ai.workley.gateway.chat.infrastructure.idempotency.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface R2dbcIdempotencyRepository extends ReactiveCrudRepository<IdempotencyEntity, String> {
}
