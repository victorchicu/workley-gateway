package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

import java.util.Map;
import java.util.UUID;

public record EmbeddingSaved(String actor, String text, Map<String, Object> metadata) implements DomainEvent {

    @Override
    public Aggregation aggregation() {
        return new Aggregation(UUID.randomUUID().toString(), AggregateTypes.CHAT, this.getClass().getSimpleName());
    }
}
