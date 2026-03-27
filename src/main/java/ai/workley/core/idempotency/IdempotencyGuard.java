package ai.workley.core.idempotency;

import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.chat.model.InfrastructureErrors;
import ai.workley.core.chat.model.Payload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IdempotencyGuard {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyGuard.class);

    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

    public IdempotencyGuard(IdempotencyStore idempotencyStore, ObjectMapper objectMapper) {
        this.idempotencyStore = idempotencyStore;
        this.objectMapper = objectMapper;
    }

    /**
     * Attempts to claim the idempotency key. Returns:
     * - empty Mono if the key is new (caller should proceed with command execution)
     * - Mono with the cached Payload if the key was already completed (caller should return it directly)
     * - error if the key is still PROCESSING (concurrent duplicate)
     */
    public Mono<Payload> tryAcquire(String key) {
        if (key == null) {
            return Mono.empty();
        }

        Idempotency idempotency = new Idempotency()
                .setId(key)
                .setState(IdempotencyState.PROCESSING);

        return idempotencyStore.saveIdempotency(idempotency)
                .then(Mono.<Payload>empty())
                .onErrorResume(InfrastructureErrors::isDuplicateKey, throwable ->
                        idempotencyStore.findIdempotencyByKey(key)
                                .flatMap(existing -> {
                                    if (existing.getState() == IdempotencyState.COMPLETED
                                            && existing.getResponseBody() != null) {
                                        return deserializePayload(existing.getResponseBody());
                                    }
                                    if (existing.getState() == IdempotencyState.PROCESSING) {
                                        return Mono.error(new ApplicationError(
                                                "Request is already being processed. Please wait."));
                                    }
                                    // FAILED state — allow retry
                                    return Mono.empty();
                                })
                );
    }

    /**
     * Marks the idempotency key as completed and stores the serialized response payload.
     */
    public Mono<Void> markCompleted(String key, Payload payload) {
        if (key == null) {
            return Mono.empty();
        }
        return idempotencyStore.findIdempotencyByKey(key)
                .switchIfEmpty(Mono.error(new IllegalStateException("Idempotency key not found: " + key)))
                .flatMap(existing -> {
                    String json = serializePayload(payload);
                    existing.setState(IdempotencyState.COMPLETED)
                            .setResponseBody(json);
                    return idempotencyStore.saveIdempotency(existing);
                })
                .then();
    }

    /**
     * Marks the idempotency key as failed so a retry with the same key can re-execute.
     */
    public Mono<Void> markFailed(String key) {
        if (key == null) {
            return Mono.empty();
        }
        return idempotencyStore.findIdempotencyByKey(key)
                .flatMap(existing -> {
                    existing.setState(IdempotencyState.FAILED);
                    return idempotencyStore.saveIdempotency(existing);
                })
                .then();
    }

    private String serializePayload(Payload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotency response", e);
        }
    }

    private Mono<Payload> deserializePayload(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, Payload.class));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached idempotency response", e);
            return Mono.empty();
        }
    }
}
