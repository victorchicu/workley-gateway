package ai.workley.gateway.chat.application.ports.outbound.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.eventstore.mongodb.EventDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Mono<EventDocument<T>> saveEvent(EventDocument<T> eventDocument);

    <T extends DomainEvent> Mono<EventDocument<T>> findLastEvent(String type, String id);

    <T extends DomainEvent> Flux<EventDocument<T>> findRecentEvents(String aggregateType, String aggregateId);
}