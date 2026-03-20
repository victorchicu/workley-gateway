package ai.workley.gateway.chat.infrastructure.eventstore.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface R2dbcEventRepository extends ReactiveCrudRepository<EventEntity, Long> {

    Flux<EventEntity> findAllByAggregateTypeAndAggregateIdOrderByVersionAsc(String aggregateType, String aggregateId);

    Mono<EventEntity> findFirstByAggregateTypeAndAggregateIdOrderByVersionDesc(String aggregateType, String aggregateId);
}
