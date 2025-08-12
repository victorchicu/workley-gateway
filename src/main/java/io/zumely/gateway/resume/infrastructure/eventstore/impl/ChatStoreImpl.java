package io.zumely.gateway.resume.infrastructure.eventstore.impl;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.EventRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatStoreImpl implements ChatStore {

    private final EventRepository eventRepository;

    public ChatStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends ApplicationEvent> Mono<StoreEvent<T>> save(String actor, T object) {
        StoreEvent<T> storeEvent =
                new StoreEvent<T>()
                        .setEventData(object);

        return eventRepository.save(storeEvent);
    }
}
