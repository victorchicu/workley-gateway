package ai.workley.gateway.chat.infrastructure.eventstore.mongodb;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MongoEventRepository extends ReactiveMongoRepository<EventDocument<?>, String> {

    <T extends DomainEvent> Flux<EventDocument<T>> findAllByAggregateTypeAndAggregateIdOrderByVersionAsc(String aggregateType, String aggregateId);

    <T extends DomainEvent> Mono<EventDocument<T>> findFirstByAggregateTypeAndAggregateIdOrderByVersionDesc(String aggregateType, String aggregateId);
}
