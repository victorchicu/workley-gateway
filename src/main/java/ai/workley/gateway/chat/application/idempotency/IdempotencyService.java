package ai.workley.gateway.chat.application.idempotency;

import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import ai.workley.gateway.chat.domain.idempotency.Idempotency;
import ai.workley.gateway.chat.application.ports.outbound.idempotency.IdempotencyStore;
import ai.workley.gateway.chat.domain.idempotency.IdempotencyState;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class IdempotencyService {
    private final IdempotencyStore idempotencyStore;

    public IdempotencyService(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    public Mono<Idempotency> start(String idempotencyKey) {
        Idempotency idempotency = new Idempotency()
                .setId(idempotencyKey)
                .setState(IdempotencyState.PROCESSING);

        return idempotencyStore.saveIdempotency(idempotency)
                .onErrorResume(InfrastructureErrors::isDuplicateKey,
                        throwable -> idempotencyStore.findIdempotencyByKey(idempotencyKey));
    }

    public Mono<Idempotency> complete(String idempotencyKey) {
        return idempotencyStore.findIdempotencyByKey(idempotencyKey)
                .map(idempotency -> idempotency.setState(IdempotencyState.COMPLETED))
                .flatMap(idempotencyStore::saveIdempotency);
    }
}