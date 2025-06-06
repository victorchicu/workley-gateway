package app.awaytogo.gateway.resume.infrastructure.eventstore.impl;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.domain.exception.InfrastructureException;
import app.awaytogo.gateway.resume.infrastructure.eventstore.*;
import app.awaytogo.gateway.resume.infrastructure.messaging.KafkaEventPublisher;
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
public class MongoEventStore implements EventStore {
    private static final Logger log = LoggerFactory.getLogger(MongoEventStore.class);

    private final EventSerializer eventSerializer;
    private final KafkaEventPublisher eventPublisher;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    private static final String EVENT_COLLECTION = "events";
    private static final String SNAPSHOT_COLLECTION = "snapshots";

    public MongoEventStore(EventSerializer eventSerializer, KafkaEventPublisher eventPublisher, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.eventSerializer = eventSerializer;
        this.eventPublisher = eventPublisher;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @Override
    public Mono<Void> saveEvents(String aggregateId, List<DomainEvent> events, Long expectedVersion) {
        if (events.isEmpty()) {
            return Mono.empty();
        }
        return checkOptimisticLock(aggregateId, expectedVersion)
                .then(Mono.defer(() -> {
                    List<EventDocument> eventDocuments = new ArrayList<>();
                    long version = expectedVersion != null ? expectedVersion : 0L;

                    for (DomainEvent event : events) {
                        version++;
                        EventDocument eventDocument = EventDocument.builder()
                                .aggregateId(aggregateId)
                                .eventType(event.getClass().getSimpleName())
                                .eventData(eventSerializer.serialize(event))
                                .eventVersion(version)
                                .timestamp(event.getTimestamp())
                                .build();
                        eventDocuments.add(eventDocument);
                    }

                    return reactiveMongoTemplate.insertAll(eventDocuments)
                            .collectList()
                            .flatMap(saved -> publishEvents(events))
                            .then();
                }))
                .doOnSuccess(v -> log.debug("Saved {} events for aggregate {}", events.size(), aggregateId))
                .doOnError(error -> log.error("Failed to save events for aggregate {}: {}", aggregateId, error.getMessage()));
    }

    @Override
    public Flux<DomainEvent> getEvents(String aggregateId) {
        Query query = Query.query(Criteria.where("aggregateId").is(aggregateId))
                .with(Sort.by(Sort.Direction.ASC, "eventVersion"));

        return reactiveMongoTemplate.find(query, EventDocument.class, EVENT_COLLECTION)
                .map(eventDocument -> eventSerializer.deserialize(eventDocument.getEventData(), eventDocument.getEventType()))
                .doOnComplete(() -> log.debug("Loaded all events for aggregate {}", aggregateId));
    }

    @Override
    public Flux<DomainEvent> getEvents(String aggregateId, Long fromVersion) {
        Query query = Query.query(
                Criteria.where("aggregateId").is(aggregateId)
                        .and("eventVersion").gt(fromVersion)
        ).with(Sort.by(Sort.Direction.ASC, "eventVersion"));

        return reactiveMongoTemplate.find(query, EventDocument.class, EVENT_COLLECTION)
                .map(doc -> eventSerializer.deserialize(doc.getEventData(), doc.getEventType()));
    }

    @Override
    public Mono<AggregateSnapshot> getLatestSnapshot(String aggregateId) {
        Query query = Query.query(Criteria.where("aggregateId").is(aggregateId).orOperator(Criteria.where("resumeId").is(aggregateId)))
                .with(Sort.by(Sort.Direction.DESC, "version"))
                .limit(1);

        return reactiveMongoTemplate.findOne(query, SnapshotDocument.class, SNAPSHOT_COLLECTION)
                .map(snapshotDocument ->
                        AggregateSnapshot.builder()
                                .aggregateId(snapshotDocument.getAggregateId())
                                .aggregateData(snapshotDocument.getAggregateData())
                                .version(snapshotDocument.getVersion())
                                .timestamp(snapshotDocument.getTimestamp())
                                .build()
                )
                .doOnSuccess(snapshot -> {
                    if (snapshot != null) {
                        log.debug("Found snapshot for aggregate {} at version {}", aggregateId, snapshot.getVersion());
                    }
                });
    }

    @Override
    public Mono<Void> saveSnapshot(AggregateSnapshot snapshot) {
        SnapshotDocument document = SnapshotDocument.builder()
                .aggregateId(snapshot.getAggregateId())
                .aggregateType("ResumeAggregate")
                .aggregateData(snapshot.getAggregateData())
                .version(snapshot.getVersion())
                .timestamp(snapshot.getTimestamp())
                .build();

        return reactiveMongoTemplate.save(document, SNAPSHOT_COLLECTION)
                .doOnSuccess(saved -> log.debug("Saved snapshot for aggregate {} at version {}",
                        snapshot.getAggregateId(), snapshot.getVersion()))
                .then();
    }

    private Mono<Void> checkOptimisticLock(String aggregateId, Long expectedVersion) {
        if (expectedVersion == null || expectedVersion == 0) {
            return Mono.empty();
        }

        Query query = Query.query(Criteria.where("aggregateId").is(aggregateId))
                .with(Sort.by(Sort.Direction.DESC, "eventVersion"))
                .limit(1);

        return reactiveMongoTemplate.findOne(query, EventDocument.class, EVENT_COLLECTION)
                .map(EventDocument::getEventVersion)
                .defaultIfEmpty(0L)
                .flatMap(currentVersion -> {
                    if (!currentVersion.equals(expectedVersion)) {
                        return Mono.error(new InfrastructureException(
                                String.format("Version conflict for aggregate %s. Expected: %d, Current: %d",
                                        aggregateId, expectedVersion, currentVersion)
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
