package app.awaytogo.gateway.resume.infrastructure.eventstore;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ResumeEventStore {
    Mono<Void> saveEvents(String resumeId, List<DomainEvent> events, Long version);

    Flux<DomainEvent> getEvents(String resumeId);

    Flux<DomainEvent> getEvents(String resumeId, Long version);

    Mono<Void> saveSnapshot(ResumeAggregateSnapshot snapshot);

    Mono<ResumeAggregateSnapshot> getLatestSnapshot(String resumeId);
}