package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.event.Event;
import io.zumely.gateway.resume.infrastructure.eventstore.EventRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.objects.StoredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EventStoreImpl implements EventStore {
    private static final Logger log = LoggerFactory.getLogger(EventStoreImpl.class);

    private final EventRepository eventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public EventStoreImpl(
            EventRepository eventRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public Mono<Void> save(Event event) {
        log.info("Save event: {} for aggregate: {}",
                event.getClass().getSimpleName(), event.getAggregateId());

        StoredEvent storedEvent = new StoredEvent(event);

        return eventRepository.save(storedEvent)
//                .doOnSuccess(applicationEventPublisher::publishEvent)
                .then();
    }

    public Flux<StoredEvent> findEventsByAggregateId(String aggregateId) {
        log.info("Finding events by aggregateId: {}", aggregateId);
        return eventRepository.findByAggregateId(aggregateId);
    }
}
