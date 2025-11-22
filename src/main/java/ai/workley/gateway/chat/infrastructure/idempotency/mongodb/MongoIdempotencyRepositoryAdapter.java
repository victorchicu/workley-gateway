package ai.workley.gateway.chat.infrastructure.idempotency.mongodb;

import ai.workley.gateway.chat.domain.idempotency.Idempotency;
import ai.workley.gateway.chat.application.ports.outbound.idempotency.IdempotencyStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MongoIdempotencyRepositoryAdapter implements IdempotencyStore {
    private final MongoIdempotencyRepository mongoIdempotencyRepository;

    public MongoIdempotencyRepositoryAdapter(MongoIdempotencyRepository mongoIdempotencyRepository) {
        this.mongoIdempotencyRepository = mongoIdempotencyRepository;
    }

    @Override
    public Mono<Idempotency> saveIdempotency(Idempotency idempotency) {
        IdempotencyDocument document =
                toIdempotencyDocument(idempotency);

        return mongoIdempotencyRepository.save(document)
                .map(this::toIdempotency);
    }

    @Override
    public Mono<Idempotency> findIdempotencyByKey(String idempotencyKey) {
        return mongoIdempotencyRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toIdempotency);
    }

    private Idempotency toIdempotency(IdempotencyDocument document) {
        return new Idempotency()
                .setId(document.getIdempotencyKey())
                .setState(document.getState())
                .setResourceId(document.getResourceId());
    }

    private IdempotencyDocument toIdempotencyDocument(Idempotency idempotency) {
        return new IdempotencyDocument()
                .setIdempotencyKey(idempotency.getId())
                .setState(idempotency.getState())
                .setResourceId(idempotency.getResourceId());
    }
}