package ai.workley.gateway.chat.application.ports.outbound.idempotency;

import ai.workley.gateway.chat.domain.idempotency.Idempotency;
import reactor.core.publisher.Mono;

public interface IdempotencyStore {

    Mono<Idempotency> saveIdempotency(Idempotency idempotency);

    Mono<Idempotency> findIdempotencyByKey(String idempotencyKey);
}