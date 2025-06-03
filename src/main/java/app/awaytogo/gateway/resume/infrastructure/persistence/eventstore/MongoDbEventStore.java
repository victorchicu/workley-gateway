package app.awaytogo.gateway.resume.infrastructure.persistence.eventstore;

import app.awaytogo.gateway.resume.domain.DomainEvent;
import app.awaytogo.gateway.resume.infrastructure.persistence.exception.EventDeserializationException;
import app.awaytogo.gateway.resume.infrastructure.persistence.exception.EventSerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MongoDbEventStore implements EventStore {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDbEventStore.class);

    private final ObjectMapper objectMapper; // For serializing/deserializing event payloads
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public MongoDbEventStore(ReactiveMongoTemplate reactiveMongoTemplate, ObjectMapper objectMapper) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        // Configure ObjectMapper if not already configured globally (e.g., for JavaTimeModule)
        // This instance should be the same as used elsewhere for consistency.
        this.objectMapper = objectMapper.copy().registerModule(new JavaTimeModule());
    }

    @Override
    public Flux<DomainEvent> getEventsForAggregate(String aggregateId) {
        LOG.debug("Fetching events for aggregateId: {}", aggregateId);
        Query query = new Query(Criteria.where("aggregateId").is(aggregateId))
                .with(Sort.by(Sort.Direction.ASC, "sequenceNumber"));
        return reactiveMongoTemplate.find(query, StoredEvent.class)
                .flatMap(storedEvent -> {
                    try {
                        // Assuming storedEvent.toDomainEvent returns DomainEvent
                        return Mono.just(storedEvent.toDomainEvent(objectMapper));
                    } catch (Exception e) {
                        LOG.error("Failed to deserialize event {} for aggregateId {}", storedEvent.getId(), aggregateId, e);
                        // Explicitly specify the type for Mono.error
                        return Mono.<DomainEvent>error(new EventDeserializationException("Failed to deserialize event: " + storedEvent.getId(), e));
                    }
                })
                .doOnError(error -> LOG.error("Error fetching events for aggregateId: {}", aggregateId, error))
                .doOnComplete(() -> LOG.debug("Successfully fetched events for aggregateId: {}", aggregateId));
    }

    @Override
    public Flux<Void> saveEvents(String aggregateId, List<? extends DomainEvent> domainEvents, long expectedVersion) {
        if (domainEvents == null || domainEvents.isEmpty()) {
            LOG.debug("No events to save for aggregateId: {}", aggregateId);
            return Flux.empty();
        }
        LOG.info("Attempting to save {} events for aggregateId: {} with expectedVersion: {}", domainEvents.size(), aggregateId, expectedVersion);

        // Determine the starting sequence number for this batch of events.
        // It should be expectedVersion + 1.
        AtomicLong currentSequenceNumber = new AtomicLong(expectedVersion + 1);
        List<StoredEvent> storedEvents = new ArrayList<>();

        for (DomainEvent domainEvent : domainEvents) {
            try {
                long seqNum = currentSequenceNumber.getAndIncrement();
                StoredEvent storedEvent = StoredEvent.fromDomainEvent(domainEvent, seqNum, objectMapper);
                storedEvents.add(storedEvent);
            } catch (Exception e) {
                LOG.error("Failed to serialize domain event for aggregateId: {}", aggregateId, e);
                return Flux.error(new EventSerializationException("Failed to serialize event: " + domainEvent.getEventType(), e));
            }
        }

        // The optimistic lock check:
        // 1. Find the current max sequence number for the aggregate.
        // 2. If it's not equal to expectedVersion, then there's a concurrency conflict.
        // This is a simplified check. A more robust way is to rely on the unique index
        // (aggregateId, sequenceNumber) and handle DuplicateKeyException.
        // If using a separate document for aggregate metadata (version), check that first.

        return reactiveMongoTemplate.find(Query.query(Criteria.where("aggregateId").is(aggregateId)).with(Sort.by(Sort.Direction.DESC, "sequenceNumber")).limit(1), StoredEvent.class)
                .map(StoredEvent::getSequenceNumber)
                .defaultIfEmpty(-1L) // If no events exist, current version is -1 (or 0 for some conventions)
                .flatMap(currentDbVersion -> {
                    if (currentDbVersion != expectedVersion) {
                        LOG.warn("Optimistic lock failed for aggregateId: {}. Expected version: {}, DB version: {}",
                                aggregateId, expectedVersion, currentDbVersion);
                        return Mono.error(new OptimisticLockingFailureException(
                                "Optimistic lock failed for aggregate " + aggregateId +
                                        ". Expected version: " + expectedVersion + ", but was: " + currentDbVersion
                        ));
                    }
                    LOG.debug("Optimistic lock check passed for aggregateId: {}. Proceeding to save {} events.", aggregateId, storedEvents.size());
                    return reactiveMongoTemplate.insertAll(storedEvents)
                            .doOnNext(savedEvent -> LOG.trace("Saved StoredEvent: id={}, aggId={}, seq={}", savedEvent.getId(), savedEvent.getAggregateId(), savedEvent.getSequenceNumber()))
                            .then()
                            .doOnSuccess(v -> LOG.info("Successfully saved {} events for aggregateId: {}", storedEvents.size(), aggregateId))
                            .doOnError(DuplicateKeyException.class, e -> {
                                // This can happen if the optimistic lock check above has a race condition
                                // or if sequence numbers are somehow miscalculated.
                                LOG.error("Duplicate key error (likely sequence number conflict) saving events for aggregateId: {}. This indicates a concurrency issue or bug.", aggregateId, e);
                            })
                            .doOnError(e -> !(e instanceof OptimisticLockingFailureException), // Avoid double logging for optimistic lock
                                    e -> LOG.error("Error saving events to MongoDB for aggregateId: {}", aggregateId, e));
                });
    }
}
