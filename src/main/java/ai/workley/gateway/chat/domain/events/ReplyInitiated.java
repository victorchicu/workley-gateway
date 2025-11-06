package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record ReplyInitiated(String actor, String chatId) implements DomainEvent {
    @Override
    public String eventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String aggregateId() {
        return "";
    }

    @Override
    public String aggregateType() {
        return AggregateTypes.CHAT;
    }
}