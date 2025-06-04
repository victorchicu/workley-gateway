package app.awaytogo.command.infrastructure.repository;

import app.awaytogo.command.domain.aggregate.ResumeAggregate;
import app.awaytogo.command.domain.event.DomainEvent;
import app.awaytogo.command.infrastructure.eventstore.AggregateSnapshot;
import app.awaytogo.command.infrastructure.eventstore.EventStore;
import app.awaytogo.command.infrastructure.eventstore.SnapshotStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Component
public class AggregateRepository {
    private static final Logger log = LoggerFactory.getLogger(AggregateRepository.class);

    private final EventStore eventStore;
    private final SnapshotStrategy snapshotStrategy;

    public AggregateRepository(EventStore eventStore, SnapshotStrategy snapshotStrategy) {
        this.eventStore = eventStore;
        this.snapshotStrategy = snapshotStrategy;
    }

    private static final int SNAPSHOT_FREQUENCY = 10; // Take snapshot every 10 events

    public Mono<ResumeAggregate> load(String aggregateId) {
        return eventStore.getLatestSnapshot(aggregateId)
                .flatMap(snapshot -> loadFromSnapshot(aggregateId, snapshot))
                .switchIfEmpty(loadFromEvents(aggregateId))
                .doOnSuccess(aggregate -> {
                    if (aggregate != null) {
                        log.debug("Loaded aggregate {} with version {}", aggregateId, aggregate.getVersion());
                    }
                });
    }

    public Mono<Void> save(ResumeAggregate aggregate, List<DomainEvent> newEvents) {
        String aggregateId = aggregate.getId();
        Long currentVersion = aggregate.getVersion();

        return eventStore.saveEvents(aggregateId, newEvents, currentVersion)
                .then(Mono.defer(() -> {
                    // Update aggregate version
                    aggregate.setVersion(currentVersion + newEvents.size());

                    // Check if we need to take a snapshot
                    if (shouldTakeSnapshot(aggregate)) {
                        return takeSnapshot(aggregate);
                    }
                    return Mono.empty();
                }))
                .doOnSuccess(v -> log.debug("Saved aggregate {} with {} new events", aggregateId, newEvents.size()));
    }

    private Mono<ResumeAggregate> loadFromSnapshot(String aggregateId, AggregateSnapshot snapshot) {
        // Deserialize aggregate from snapshot
        ResumeAggregate aggregate = snapshotStrategy.deserialize(snapshot.getAggregateData());
        aggregate.setVersion(snapshot.getVersion());

        // Load events after snapshot
        return eventStore.getEvents(aggregateId, snapshot.getVersion())
                .collectList()
                .map(events -> {
                    events.forEach(aggregate::apply);
                    aggregate.setVersion(snapshot.getVersion() + events.size());
                    return aggregate;
                });
    }

    private Mono<ResumeAggregate> loadFromEvents(String aggregateId) {
        return eventStore.getEvents(aggregateId)
                .collectList()
                .map(events -> {
                    if (events.isEmpty()) {
                        return null;
                    }
                    ResumeAggregate aggregate = new ResumeAggregate();
                    events.forEach(aggregate::apply);
                    aggregate.setVersion((long) events.size());
                    return aggregate;
                });
    }

    private boolean shouldTakeSnapshot(ResumeAggregate aggregate) {
        return aggregate.getVersion() % SNAPSHOT_FREQUENCY == 0;
    }

    private Mono<Void> takeSnapshot(ResumeAggregate aggregate) {
        AggregateSnapshot snapshot = AggregateSnapshot.builder()
                .aggregateId(aggregate.getId())
                .aggregateData(snapshotStrategy.serialize(aggregate))
                .version(aggregate.getVersion())
                .timestamp(Instant.now())
                .build();

        return eventStore.saveSnapshot(snapshot)
                .doOnSuccess(v -> log.debug("Created snapshot for aggregate {} at version {}",
                        aggregate.getId(), aggregate.getVersion()));
    }
}
