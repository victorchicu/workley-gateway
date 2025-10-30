package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.shared.domain.aggregations.AggregateTypes;
import ai.workley.gateway.features.shared.domain.event.DomainEvent;

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
