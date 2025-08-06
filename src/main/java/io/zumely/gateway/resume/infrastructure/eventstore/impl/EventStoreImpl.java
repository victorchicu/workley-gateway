package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.service.EventSerializer;
import io.zumely.gateway.resume.application.event.Event;
import io.zumely.gateway.resume.infrastructure.eventstore.EventRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.objects.StoredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Service
public class EventStoreImpl implements EventStore {
    private static final Logger log = LoggerFactory.getLogger(EventStoreImpl.class);

    private final EventRepository eventRepository;
    private final EventSerializer eventSerializer;

    public EventStoreImpl(
            EventRepository eventRepository,
            EventSerializer eventSerializer
    ) {
        this.eventRepository = eventRepository;
        this.eventSerializer = eventSerializer;
    }

    public Mono<StoredEvent> save(Event event) {
        StoredEvent storedEvent =
                new StoredEvent(event).setData(eventSerializer.serialize(event));

        return eventRepository.save(storedEvent);
    }

    public Flux<StoredEvent> findEvents(Principal principal, String chatId) {
        log.info("Finding events by chat ID: {}", chatId);
        return eventRepository.findAllByPrincipalAndChatId(chatId);
    }
}