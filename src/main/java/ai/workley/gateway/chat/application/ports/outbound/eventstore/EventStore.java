package ai.workley.gateway.chat.application.ports.outbound.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.domain.events.EventEnvelope;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Mono<EventEnvelope<T>> saveEvent(EventEnvelope<T> envelope);

    <T extends DomainEvent> Mono<EventEnvelope<T>> findLastEvent(String aggregateType, String aggregateId);

    <T extends DomainEvent> Flux<EventEnvelope<T>> findRecentEvents(String aggregateType, String aggregateId);
}
