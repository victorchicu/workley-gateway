package ai.workley.core.chat.repository;

import ai.workley.core.chat.service.EventStore;
import ai.workley.core.chat.model.DomainEvent;
import ai.workley.core.chat.model.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class R2dbcEventRepositoryAdapter implements EventStore {
    private final R2dbcEventRepository repository;
    private final ObjectMapper objectMapper;

    public R2dbcEventRepositoryAdapter(R2dbcEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T extends DomainEvent> Mono<EventEnvelope<T>> saveEvent(EventEnvelope<T> envelope) {
        EventEntity entity = toEntity(envelope);
        return repository.save(entity)
                .map(saved -> toEnvelope(saved, envelope.eventData()));
    }

    @Override
    public <T extends DomainEvent> Mono<EventEnvelope<T>> findLastEvent(String aggregateType, String aggregateId) {
        return repository.findFirstByAggregateTypeAndAggregateIdOrderByVersionDesc(aggregateType, aggregateId)
                .map(this::toEnvelope);
    }

    @Override
    public <T extends DomainEvent> Flux<EventEnvelope<T>> findRecentEvents(String aggregateType, String aggregateId) {
        return repository.findAllByAggregateTypeAndAggregateIdOrderByVersionAsc(aggregateType, aggregateId)
                .map(this::toEnvelope);
    }

    private <T extends DomainEvent> EventEnvelope<T> toEnvelope(EventEntity entity, T eventData) {
        return new EventEnvelope<>(
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getVersion(),
                entity.getEventType(),
                eventData
        );
    }

    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> EventEnvelope<T> toEnvelope(EventEntity entity) {
        try {
            T eventData = (T) objectMapper.readValue(entity.getEventData().asString(), DomainEvent.class);
            return new EventEnvelope<>(
                    entity.getAggregateType(),
                    entity.getAggregateId(),
                    entity.getVersion(),
                    entity.getEventType(),
                    eventData
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize event data", e);
        }
    }

    private <T extends DomainEvent> EventEntity toEntity(EventEnvelope<T> envelope) {
        try {
            String json = objectMapper.writeValueAsString(envelope.eventData());
            return new EventEntity()
                    .setAggregateType(envelope.aggregateType())
                    .setAggregateId(envelope.aggregateId())
                    .setVersion(envelope.version())
                    .setEventType(envelope.eventType())
                    .setEventData(Json.of(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event data", e);
        }
    }
}
