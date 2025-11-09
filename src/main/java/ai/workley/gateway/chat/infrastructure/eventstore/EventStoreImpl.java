package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.application.ports.outbound.EventStore;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.eventstore.mongodb.EventDocument;
import ai.workley.gateway.chat.infrastructure.exceptions.ConcurrencyException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class EventStoreImpl implements EventStore {
    private final EventRepository eventRepository;

    public EventStoreImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public <T extends DomainEvent> Flux<EventDocument<T>> load(String aggregateType, String aggregateId) {
        return eventRepository.findRecentEvents(aggregateType, aggregateId);
    }

    @Override
    public <T extends DomainEvent> Mono<EventDocument<T>> append(String actor, T data, Long expectedVersion) {
        return eventRepository
                .findLastEvent(data.aggregation().type(), data.aggregation().id())
                .map(EventDocument::getVersion)
                .defaultIfEmpty(-1L)
                .flatMap(currentVersion -> {
                    if (expectedVersion != null && !Objects.equals(expectedVersion, currentVersion)) {
                        return Mono.error(
                                new ConcurrencyException(
                                        data.aggregation().type(), data.aggregation().id(), expectedVersion, currentVersion)
                        );
                    }
                    long nextVersion = currentVersion + 1;
                    EventDocument<T> eventDocument =
                            new EventDocument<T>()
                                    .setEventType(data.aggregation().event())
                                    .setAggregateId(data.aggregation().id())
                                    .setAggregateType(data.aggregation().type())
                                    .setVersion(nextVersion)
                                    .setEventData(data);

                    return eventRepository.saveEvent(eventDocument);
                });
    }
}
