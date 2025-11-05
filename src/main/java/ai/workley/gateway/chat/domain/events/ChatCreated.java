package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record ChatCreated(String actor, String chatId, String prompt) implements DomainEvent {

    @Override
    public String eventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String aggregateId() {
        return chatId;
    }

    @Override
    public String aggregateType() {
        return AggregateTypes.CHAT;
    }
}
