package ai.workley.core.chat.service;

import ai.workley.core.chat.model.Idempotency;
import reactor.core.publisher.Mono;

public interface IdempotencyStore {

    Mono<Idempotency> saveIdempotency(Idempotency idempotency);

    Mono<Idempotency> findIdempotencyByKey(String idempotencyKey);
}
