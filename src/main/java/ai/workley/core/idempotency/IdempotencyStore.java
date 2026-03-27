package ai.workley.core.idempotency;
import reactor.core.publisher.Mono;

public interface IdempotencyStore {

    Mono<Idempotency> saveIdempotency(Idempotency idempotency);

    Mono<Idempotency> findIdempotencyByKey(String idempotencyKey);
}
