package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Flux<EventDocument<T>> load(String aggregateType, String aggregateId);

    <T extends DomainEvent> Mono<EventDocument<T>> append(String actor, T object, Long expectedVersion);
}
