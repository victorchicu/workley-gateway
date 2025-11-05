package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import com.github.f4b6a3.tsid.Tsid;
import com.github.f4b6a3.tsid.TsidCreator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventStoreImpl implements EventStore {
    private static final Tsid tsid = TsidCreator.getTsid();

    private final EventRepository eventRepository;

    public EventStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends DomainEvent> Mono<EventDocument<T>> save(String actor, T data) {
        EventDocument<T> eventDocument =
                new EventDocument<T>()
                        .setEventType(data.eventType())
                        .setAggregateId(data.aggregateId())
                        .setAggregateType(data.aggregateType())
                        .setVersion(tsid.toLong())
                        .setEventData(data);

        return eventRepository.save(eventDocument);
    }
}
