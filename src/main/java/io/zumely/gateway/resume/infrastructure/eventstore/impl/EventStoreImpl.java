package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.event.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.EventRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoredEvent;
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

    public <T extends ApplicationEvent> Mono<StoredEvent<T>> saveEvent(Principal actor, T applicationEvent) {
        StoredEvent<T> storedEvent =
                new StoredEvent<T>()
                        .setActor(actor.getName())
                        .setData(applicationEvent);

        return eventRepository.save(storedEvent);
    }

    @SuppressWarnings("unchecked")
    public <T extends ApplicationEvent> Flux<StoredEvent<T>> getChatHistory(Principal actor, String chatId) {
        return eventRepository.findEventsByChatId(actor.getName(), chatId)
                .map((StoredEvent<?> event) -> (StoredEvent<T>) event);
    }
}
