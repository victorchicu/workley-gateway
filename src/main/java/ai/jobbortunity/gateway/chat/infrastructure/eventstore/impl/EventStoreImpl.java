package ai.jobbortunity.gateway.chat.infrastructure.eventstore.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventRepository;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import ai.jobbortunity.gateway.chat.infrastructure.data.EventObject;
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
    public <T extends ApplicationEvent> Mono<EventObject<T>> save(String actor, T data) {
        EventObject<T> eventObject =
                new EventObject<T>()
                        .setEventData(data);

        return eventRepository.save(eventObject);
    }
}
