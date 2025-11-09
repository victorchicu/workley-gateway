package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.application.ports.outbound.EventStore;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.eventstore.mongodb.EventDocument;
import ai.workley.gateway.chat.infrastructure.exceptions.ConcurrencyException;
import org.springframework.stereotype.Component;
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
    public <T extends DomainEvent> Mono<EventDocument<T>> append(String actor, T data, String aggregateType, String aggregateId, Long expectedVersion) {
        return eventRepository
                .findLastEvent(aggregateType, aggregateId)
                .map(EventDocument::getVersion)
                .defaultIfEmpty(-1L)
                .flatMap(currentVersion -> {
                    if (expectedVersion != null && !Objects.equals(expectedVersion, currentVersion)) {
                        return Mono.error(
                                new ConcurrencyException(
                                        aggregateType, aggregateId, expectedVersion, currentVersion)
                        );
                    }
                    long nextVersion = currentVersion + 1;
                    EventDocument<T> eventDocument =
                            new EventDocument<T>()
                                    .setEventType(data.getClass().getSimpleName())
                                    .setAggregateId(aggregateId)
                                    .setAggregateType(aggregateType)
                                    .setVersion(nextVersion)
                                    .setEventData(data);

                    return eventRepository.saveEvent(eventDocument);
                });
    }
}
