package ai.zumely.gateway.resume.infrastructure.eventstore;

import ai.zumely.gateway.resume.domain.aggregate.ResumeAggregate;
import ai.zumely.gateway.resume.domain.event.DomainEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ResumeEventStore {
    Mono<Void> saveEvents(ResumeAggregate aggregate, List<DomainEvent> events);

    Flux<DomainEvent> getEvents(String resumeId);

    Flux<DomainEvent> getEvents(String resumeId, Long version);

    Mono<Void> saveSnapshot(ResumeAggregateSnapshot snapshot);

    Mono<ResumeAggregateSnapshot> getLatestSnapshot(String resumeId);
}