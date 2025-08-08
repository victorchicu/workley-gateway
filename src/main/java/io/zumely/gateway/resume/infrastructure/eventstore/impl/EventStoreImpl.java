package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.event.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.EventRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.entity.StoredEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Service
public class EventStoreImpl implements EventStore {

    private final EventRepository eventRepository;

    public EventStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public <T extends ApplicationEvent> Mono<StoredEvent<T>> save(Principal actor, T applicationEvent) {
        StoredEvent<T> storedEvent =
                new StoredEvent<T>()
                        .setActor(actor.getName())
                        .setEvent(applicationEvent);

        return eventRepository.save(storedEvent);
    }

    @SuppressWarnings("unchecked")
    public <T extends ApplicationEvent> Flux<StoredEvent<T>> findEvents(Principal actor, String chatId) {
        return eventRepository.findAllByActor(actor.getName())
                .map((StoredEvent<?> event) -> (StoredEvent<T>) event);
    }
}