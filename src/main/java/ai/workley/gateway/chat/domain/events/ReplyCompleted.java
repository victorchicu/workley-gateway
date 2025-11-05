package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

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
