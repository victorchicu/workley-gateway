package app.awaytogo.gateway.resume.infrastructure.eventstore;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventStore {
    Mono<Void> saveEvents(String aggregateId, List<DomainEvent> events, Long expectedVersion);

    Flux<DomainEvent> getEvents(String aggregateId);

    Flux<DomainEvent> getEvents(String aggregateId, Long fromVersion);

    Mono<Void> saveSnapshot(AggregateSnapshot snapshot);

    Mono<AggregateSnapshot> getLatestSnapshot(String aggregateId);
}
