package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record ReplySaved(String actor, String chatId, Message<String> reply) implements DomainEvent {
    @Override
    public Aggregation aggregation() {
        return new Aggregation(chatId, AggregateTypes.CHAT, this.getClass().getSimpleName());
    }
}
