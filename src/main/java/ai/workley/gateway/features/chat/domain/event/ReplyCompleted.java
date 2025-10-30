package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.domain.aggregations.AggregateTypes;
import ai.workley.gateway.features.shared.domain.event.DomainEvent;

public record ReplyCompleted(String actor, String chatId, Message<String> reply) implements DomainEvent {

    @Override
    public String eventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String aggregateId() {
        return reply.id();
    }

    @Override
    public String aggregateType() {
        return AggregateTypes.CHAT;
    }
}
