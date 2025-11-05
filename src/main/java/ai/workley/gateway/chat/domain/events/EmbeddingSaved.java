package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

import java.util.Map;
import java.util.UUID;

public record EmbeddingSaved(String actor, String text, Map<String, Object> metadata) implements DomainEvent {

    @Override
    public String eventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String aggregateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String aggregateType() {
        return AggregateTypes.EMBEDDING;
    }
}
