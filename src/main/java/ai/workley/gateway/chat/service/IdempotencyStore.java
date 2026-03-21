package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.Idempotency;
import reactor.core.publisher.Mono;

public interface IdempotencyStore {

    Mono<Idempotency> saveIdempotency(Idempotency idempotency);

    Mono<Idempotency> findIdempotencyByKey(String idempotencyKey);
}