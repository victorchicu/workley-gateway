package ai.workley.core.idempotency;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class R2dbcIdempotencyRepositoryAdapter implements IdempotencyStore {
    private final R2dbcIdempotencyRepository repository;

    public R2dbcIdempotencyRepositoryAdapter(R2dbcIdempotencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Idempotency> saveIdempotency(Idempotency idempotency) {
        return repository.findById(idempotency.getId())
                .map(existing -> {
                    existing.setState(idempotency.getState().name());
                    existing.setResourceId(idempotency.getResourceId());
                    existing.setResponseBody(idempotency.getResponseBody());
                    existing.markExisting();
                    return existing;
                })
                .switchIfEmpty(Mono.just(toEntity(idempotency)))
                .flatMap(repository::save)
                .map(this::toIdempotency);
    }

    @Override
    public Mono<Idempotency> findIdempotencyByKey(String idempotencyKey) {
        return repository.findById(idempotencyKey)
                .map(this::toIdempotency);
    }

    private Idempotency toIdempotency(IdempotencyEntity entity) {
        return new Idempotency()
                .setId(entity.getId())
                .setState(IdempotencyState.valueOf(entity.getState()))
                .setResourceId(entity.getResourceId())
                .setResponseBody(entity.getResponseBody());
    }

    private IdempotencyEntity toEntity(Idempotency idempotency) {
        return new IdempotencyEntity()
                .setId(idempotency.getId())
                .setState(idempotency.getState().name())
                .setResourceId(idempotency.getResourceId())
                .setResponseBody(idempotency.getResponseBody());
    }
}
