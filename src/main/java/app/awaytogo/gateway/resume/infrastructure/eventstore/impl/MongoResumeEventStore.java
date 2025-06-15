package app.awaytogo.gateway.resume.infrastructure.eventstore.impl;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.infrastructure.exception.InfrastructureException;
import app.awaytogo.gateway.resume.infrastructure.eventstore.*;
import app.awaytogo.gateway.resume.infrastructure.kafka.KafkaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoResumeEventStore implements ResumeEventStore {
    private static final Logger log = LoggerFactory.getLogger(MongoResumeEventStore.class);

    private final EventSerializer eventSerializer;
    private final KafkaEventPublisher eventPublisher;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final String EVENT_COLLECTION = "resume_events";
    private static final String SNAPSHOT_COLLECTION = "resume_snapshots";

    public MongoResumeEventStore(EventSerializer eventSerializer, KafkaEventPublisher eventPublisher, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.eventSerializer = eventSerializer;
        this.eventPublisher = eventPublisher;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Void> saveEvents(String resumeId, List<DomainEvent> events, Long version) {
        if (events.isEmpty()) {
            return Mono.empty();
        }
        return checkOptimisticLock(resumeId, version)
                .then(Mono.defer(() -> {
                    List<EventDocument> eventDocuments = new ArrayList<>();
                    long nextVersion = version != null ? version : 0L;
                    for (DomainEvent event : events) {
                        nextVersion++;
                        eventDocuments.add(
                                EventDocument.builder()
                                        .type(event.getClass().getSimpleName())
                                        .data(eventSerializer.serialize(event))
                                        .version(nextVersion)
                                        .resumeId(resumeId)
                                        .build()
                        );
                    }
                    return reactiveMongoTemplate.insertAll(eventDocuments)
                            .collectList()
                            .flatMap(saved -> {
                                return publishEvents(events);
                            })
                            .then();
                }))
                .doOnSuccess(v -> {
                    log.debug("Saved {} events for aggregate {}", events.size(), resumeId);
                })
                .doOnError(error -> {
                    log.error("Failed to save events for aggregate {}: {}", resumeId, error.getMessage());
                });
    }

    @Override
    public Flux<DomainEvent> getEvents(String resumeId) {
        Query query = Query.query(Criteria.where("resumeId").is(resumeId))
                .with(Sort.by(Sort.Direction.ASC, "version"));

        return reactiveMongoTemplate.find(query, EventDocument.class, EVENT_COLLECTION)
                .map(eventDocument -> eventSerializer.deserialize(eventDocument.getData(), eventDocument.getType()))
                .doOnComplete(() -> log.debug("Loaded all events for aggregate {}", resumeId));
    }

    @Override
    public Flux<DomainEvent> getEvents(String resumeId, Long version) {
        Query query = Query.query(Criteria.where("resumeId").is(resumeId).and("version").gt(version))
                .with(Sort.by(Sort.Direction.ASC, "version"));

        return reactiveMongoTemplate.find(query, EventDocument.class, EVENT_COLLECTION)
                .map(doc -> eventSerializer.deserialize(doc.getData(), doc.getType()));
    }

    @Override
    public Mono<ResumeAggregateSnapshot> getLatestSnapshot(String resumeId) {
        Query query = Query.query(Criteria.where("resumeId").is(resumeId))
                .with(Sort.by(Sort.Direction.DESC, "version"))
                .limit(1);

        return reactiveMongoTemplate.findOne(query, SnapshotDocument.class, SNAPSHOT_COLLECTION)
                .map(snapshotDocument ->
                        ResumeAggregateSnapshot.builder()
                                .data(snapshotDocument.getData())
                                .version(snapshotDocument.getVersion())
                                .resumeId(snapshotDocument.getResumeId())
                                .timestamp(snapshotDocument.getCreatedDate())
                                .build()
                )
                .doOnSuccess(snapshot -> {
                    if (snapshot != null) {
                        log.debug("Found snapshot for aggregate {} at version {}", resumeId, snapshot.getVersion());
                    }
                });
    }

    @Override
    public Mono<Void> saveSnapshot(ResumeAggregateSnapshot snapshot) {
        SnapshotDocument document = SnapshotDocument.builder()
                .type("ResumeAggregate")
                .data(snapshot.getData())
                .version(snapshot.getVersion())
                .resumeId(snapshot.getResumeId())
                .build();

        return reactiveMongoTemplate.save(document, SNAPSHOT_COLLECTION)
                .doOnSuccess(saved -> {
                    log.debug("Saved snapshot for aggregate {} at version {}", snapshot.getResumeId(), snapshot.getVersion());
                })
                .then();
    }

    private Mono<Void> checkOptimisticLock(String resumeId, Long expectedVersion) {
        if (expectedVersion == null || expectedVersion == 0) {
            return Mono.empty();
        }

        Query query = Query.query(Criteria.where("resumeId").is(resumeId))
                .with(Sort.by(Sort.Direction.DESC, "version"))
                .limit(1);

        return reactiveMongoTemplate.findOne(query, EventDocument.class, EVENT_COLLECTION)
                .map(EventDocument::getVersion)
                .defaultIfEmpty(0L)
                .flatMap(currentVersion -> {
                    if (!currentVersion.equals(expectedVersion)) {
                        return Mono.error(new InfrastructureException(
                                String.format("Version conflict for aggregate %s. Expected: %d, Current: %d",
                                        resumeId, expectedVersion, currentVersion)
                        ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> publishEvents(List<DomainEvent> events) {
        return Flux.fromIterable(events)
                .flatMap(eventPublisher::publish)
                .then();
    }
}
