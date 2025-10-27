package ai.workley.gateway.features.chat.infra.eventstore;

import ai.workley.gateway.features.chat.domain.event.DomainEvent;
import ai.workley.gateway.features.chat.infra.readmodel.EventModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventStoreImpl implements EventStore {

    private final EventRepository eventRepository;

    public EventStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends DomainEvent> Mono<EventModel<T>> save(String actor, T data) {
        EventModel<T> eventModel =
                new EventModel<T>()
                        .setEventData(data);

        return eventRepository.save(eventModel);
    }
}
