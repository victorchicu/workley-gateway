package ai.jobbortunity.gateway.resume.infrastructure.eventstore.impl;

import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.resume.infrastructure.eventstore.EventRepository;
import ai.jobbortunity.gateway.resume.infrastructure.eventstore.EventStore;
import ai.jobbortunity.gateway.resume.infrastructure.data.EventObject;
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
