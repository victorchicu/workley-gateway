package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.domain.aggregations.AggregateTypes;
import ai.workley.gateway.features.shared.domain.event.DomainEvent;

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