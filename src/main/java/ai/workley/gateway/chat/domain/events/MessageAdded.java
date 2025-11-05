package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record MessageAdded(String actor, String chatId, Message<String> message) implements DomainEvent {

    @Override
    public String eventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String aggregateId() {
        return message.id();
    }

    @Override
    public String aggregateType() {
        return AggregateTypes.CHAT;
    }
}
