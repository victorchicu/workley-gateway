package app.awaytogo.gateway.resume.infrastructure.persistence.eventstore;

import app.awaytogo.gateway.resume.domain.DomainEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventStore {
    /**
     * Retrieves all domain events for a given aggregate ID, ordered by sequence number.
     * @param aggregateId The ID of the aggregate.
     * @return A Flux emitting the domain events.
     */
    Flux<DomainEvent> getEventsForAggregate(String aggregateId);

    /**
     * Saves a list of new domain events for an aggregate.
     * This operation should be atomic.
     * @param aggregateId The ID of the aggregate.
     * @param events The list of new domain events to save.
     * @param expectedVersion The version of the aggregate *before* these new events are applied.
     * Used for optimistic concurrency control. For a new aggregate, this is typically -1 or 0.
     * @return A Mono<Void> that completes when events are saved, or errors if optimistic lock fails or other issues.
     */
    Flux<Void> saveEvents(String aggregateId, List<? extends DomainEvent> events, long expectedVersion);
}
