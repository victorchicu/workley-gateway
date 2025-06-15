package app.awaytogo.gateway.resume.domain.service;

import app.awaytogo.gateway.resume.domain.aggregate.ResumeAggregate;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.infrastructure.eventstore.ResumeAggregateSnapshot;
import app.awaytogo.gateway.resume.infrastructure.eventstore.ResumeEventStore;
import app.awaytogo.gateway.resume.infrastructure.eventstore.SnapshotStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Component
public class ResumeAggregateService {
    private static final Logger log = LoggerFactory.getLogger(ResumeAggregateService.class);

    private final ResumeEventStore resumeEventStore;
    private final SnapshotStrategy snapshotStrategy;

    public ResumeAggregateService(ResumeEventStore resumeEventStore, SnapshotStrategy snapshotStrategy) {
        this.resumeEventStore = resumeEventStore;
        this.snapshotStrategy = snapshotStrategy;
    }

    private static final int SNAPSHOT_FREQUENCY = 10;

    public Mono<Void> save(ResumeAggregate resumeAggregate, List<DomainEvent> newEvents) {
        String resumeId = resumeAggregate.getResumeId();
        Long version = resumeAggregate.getVersion();
        return resumeEventStore.saveEvents(resumeId, newEvents, version)
                .then(Mono.defer(() -> {
                    resumeAggregate.setVersion(version + newEvents.size());
                    if (shouldTakeSnapshot(resumeAggregate)) {
                        return takeSnapshot(resumeAggregate);
                    }
                    return Mono.empty();
                }))
                .doOnSuccess(v -> log.debug("Saved resume aggregate {} with {} new events", resumeId, newEvents.size()));
    }

    public Mono<ResumeAggregate> load(String resumeId) {
        return resumeEventStore.getLatestSnapshot(resumeId)
                .flatMap(snapshot -> {
                    return loadFromSnapshot(resumeId, snapshot);
                })
                .switchIfEmpty(loadFromEvents(resumeId))
                .doOnSuccess(aggregate -> {
                    if (aggregate != null) {
                        log.debug("Loaded aggregate {} with version {}", resumeId, aggregate.getVersion());
                    }
                });
    }


    private Mono<ResumeAggregate> loadFromSnapshot(String resumeId, ResumeAggregateSnapshot resumeAggregateSnapshot) {
        ResumeAggregate resumeAggregate = snapshotStrategy.deserialize(resumeAggregateSnapshot.getData());
        resumeAggregate.setVersion(resumeAggregateSnapshot.getVersion());
        return resumeEventStore.getEvents(resumeId, resumeAggregateSnapshot.getVersion())
                .collectList()
                .map(events -> {
                    events.forEach(resumeAggregate::apply);
                    resumeAggregate.setVersion(resumeAggregateSnapshot.getVersion() + events.size());
                    return resumeAggregate;
                });
    }

    private Mono<ResumeAggregate> loadFromEvents(String resumeId) {
        return resumeEventStore.getEvents(resumeId)
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

    private Mono<Void> takeSnapshot(ResumeAggregate resumeAggregate) {
        ResumeAggregateSnapshot snapshot = ResumeAggregateSnapshot.builder()
                .data(snapshotStrategy.serialize(resumeAggregate))
                .version(resumeAggregate.getVersion())
                .resumeId(resumeAggregate.getResumeId())
                .timestamp(Instant.now())
                .build();

        return resumeEventStore.saveSnapshot(snapshot)
                .doOnSuccess(v -> log.debug("Created snapshot for aggregate {} at version {}",
                        resumeAggregate.getResumeId(), resumeAggregate.getVersion()));
    }
}
