package ai.workley.gateway.chat.application.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.domain.events.EventEnvelope;
import ai.workley.gateway.chat.application.ports.outbound.eventstore.EventStore;
import ai.workley.gateway.chat.infrastructure.exceptions.ConcurrencyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class EventService {
    private final EventStore eventStore;

    public EventService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public <T extends DomainEvent> Flux<EventEnvelope<T>> load(String aggregateType, String aggregateId) {
        return eventStore.findRecentEvents(aggregateType, aggregateId);
    }

    public <T extends DomainEvent> Mono<EventEnvelope<T>> append(T data, String aggregateType, String aggregateId, Long expectedVersion) {
        return eventStore
                .findLastEvent(aggregateType, aggregateId)
                .map(EventEnvelope::version)
                .defaultIfEmpty(-1L)
                .flatMap(currentVersion -> {
                    if (expectedVersion != null && !Objects.equals(expectedVersion, currentVersion)) {
                        return Mono.error(
                                new ConcurrencyException(
                                        aggregateType, aggregateId, expectedVersion, currentVersion)
                        );
                    }
                    long nextVersion = currentVersion + 1;
                    EventEnvelope<T> envelope = new EventEnvelope<>(
                            aggregateType,
                            aggregateId,
                            nextVersion,
                            data.getClass().getSimpleName(),
                            data
                    );
                    return eventStore.saveEvent(envelope);
                });
    }
}
