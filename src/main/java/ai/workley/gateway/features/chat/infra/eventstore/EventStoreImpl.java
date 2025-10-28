package ai.workley.gateway.features.chat.infra.eventstore;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.EventDocument;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventStoreImpl implements EventStore {

    private final EventRepository eventRepository;

    public EventStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends DomainEvent> Mono<EventDocument<T>> save(String actor, T data) {
        EventDocument<T> eventDocument =
                new EventDocument<T>()
                        .setEventData(data);

        return eventRepository.save(eventDocument);
    }
}
