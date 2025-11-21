package ai.workley.gateway.chat.infrastructure.idempotency.mongodb;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoIdempotencyRepository extends ReactiveCrudRepository<IdempotencyDocument, String> {

}