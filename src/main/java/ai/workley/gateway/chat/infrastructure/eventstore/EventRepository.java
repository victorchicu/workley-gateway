package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventDocument<?>, String> {

    <T extends DomainEvent> Flux<EventDocument<T>> findAllByAggregateTypeAndAggregateIdOrderByVersionDesc(
            String aggregateType, String aggregateId);

    <T extends DomainEvent> Mono<EventDocument<T>> findFirstByAggregateTypeAndAggregateIdOrderByVersionDesc(
            String aggregateType, String aggregateId);
}
