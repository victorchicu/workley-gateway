package ai.workley.core.idempotency;

import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.chat.model.InfrastructureErrors;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IdempotencyGuard {
    private final IdempotencyStore idempotencyStore;

    public IdempotencyGuard(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    public Mono<Idempotency> ensureIdempotency(String key, String resourceId) {
        if (key == null) {
            // idempotency disabled
            return Mono.empty();
        }

        Idempotency idempotency =
                new Idempotency().setId(key).setResourceId(resourceId).setState(IdempotencyState.PROCESSING);

        return idempotencyStore.saveIdempotency(idempotency)
                .onErrorResume(InfrastructureErrors::isDuplicateKey, throwable ->
                        idempotencyStore.findIdempotencyByKey(key)
                                .flatMap(existing -> {
                                    if (existing.getResourceId() != null
                                            && !existing.getResourceId().equals(resourceId)) {
                                        return Mono.error(
                                                new ApplicationError(
                                                        "Idempotency key '" + key + "' already used for another chat '" + existing.getResourceId() + "'."));
                                    }
                                    if (existing.getResourceId() != null) {
                                        return Mono.just(existing);
                                    }
                                    return idempotencyStore.saveIdempotency(existing.setResourceId(resourceId));
                                })
                );
    }

    public Mono<Idempotency> markIdempotentCompleted(String key, String resourceId) {
        if (key == null) {
            // idempotency disabled
            return Mono.empty();
        }
        return idempotencyStore.findIdempotencyByKey(key)
                .switchIfEmpty(Mono.error(new IllegalStateException("Idempotency key not found: " + key)))
                .map(existing -> existing.setState(IdempotencyState.COMPLETED).setResourceId(resourceId))
                .flatMap(idempotencyStore::saveIdempotency);
    }
}
