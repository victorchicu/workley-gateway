package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EventRepository extends ReactiveMongoRepository<EventDocument<?>, String> {

    @Query("{ 'aggregateType': ?0, 'aggregateId': ?1 }")
    <T extends DomainEvent> Flux<EventDocument<T>> findAll(String aggregateType, String aggregateId, Sort sort);

    @Query("{ 'aggregateType': ?0, 'aggregateId': ?1 }")
    <T extends DomainEvent> Mono<EventDocument<T>> findFirst(String aggregateType, String aggregateId, Sort sort);
}
