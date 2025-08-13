package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.EventRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.EventObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Service
public class EventStoreImpl implements EventStore {

    private final EventRepository eventRepository;

    public EventStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends ApplicationEvent> Mono<EventObject<T>> save(Principal actor, T data) {
        EventObject<T> eventObject =
                new EventObject<T>()
                        .setEventData(data);

        return eventRepository.save(eventObject);
    }
}