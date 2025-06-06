package app.awaytogo.gateway.resume.infrastructure.repository;

import app.awaytogo.gateway.resume.domain.aggregate.ResumeAggregate;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.infrastructure.eventstore.AggregateSnapshot;
import app.awaytogo.gateway.resume.infrastructure.eventstore.EventStore;
import app.awaytogo.gateway.resume.infrastructure.eventstore.SnapshotStrategy;
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

    private static final int SNAPSHOT_FREQUENCY = 10;

    public Mono<Void> save(ResumeAggregate aggregate, List<DomainEvent> newEvents) {
        String aggregateId = aggregate.getAggregateId();
        Long currentVersion = aggregate.getVersion();

        return eventStore.saveEvents(aggregateId, newEvents, currentVersion)
                .then(Mono.defer(() -> {
                    aggregate.setVersion(currentVersion + newEvents.size());
                    if (shouldTakeSnapshot(aggregate)) {
                        return takeSnapshot(aggregate);
                    }
                    return Mono.empty();
                }))
                .doOnSuccess(v -> log.debug("Saved aggregate {} with {} new events", aggregateId, newEvents.size()));
    }

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


    private Mono<ResumeAggregate> loadFromSnapshot(String aggregateId, AggregateSnapshot snapshot) {
        ResumeAggregate resumeAggregate = snapshotStrategy.deserialize(snapshot.getAggregateData());
        resumeAggregate.setVersion(snapshot.getVersion());
        return eventStore.getEvents(aggregateId, snapshot.getVersion())
                .collectList()
                .map(events -> {
                    events.forEach(resumeAggregate::apply);
                    resumeAggregate.setVersion(snapshot.getVersion() + events.size());
                    return resumeAggregate;
                });
    }

    private Mono<ResumeAggregate> loadFromEvents(String aggregateId) {
        return eventStore.getEvents(aggregateId)
                .collectList()
                .mapNotNull(events -> {
                    ResumeAggregate aggregate = ResumeAggregate.fromEvents(events);
                    aggregate.setVersion((long) events.size());
                    return aggregate;
                });
    }

    private boolean shouldTakeSnapshot(ResumeAggregate aggregate) {
        return aggregate.getVersion() %SNAPSHOT_FREQUENCY == 0;
    }

    private Mono<Void> takeSnapshot(ResumeAggregate aggregate) {
        AggregateSnapshot snapshot = AggregateSnapshot.builder()
                .aggregateId(aggregate.getAggregateId())
                .aggregateData(snapshotStrategy.serialize(aggregate))
                .version(aggregate.getVersion())
                .timestamp(Instant.now())
                .build();

        return eventStore.saveSnapshot(snapshot)
                .doOnSuccess(v -> log.debug("Created snapshot for aggregate {} at version {}",
                        aggregate.getAggregateId(), aggregate.getVersion()));
    }
}
