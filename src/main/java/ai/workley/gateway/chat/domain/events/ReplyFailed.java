package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record ReplyFailed(String actor, String chatId, String failure) implements DomainEvent {

    @Override
    public Aggregation aggregation() {
        return new Aggregation(chatId, AggregateTypes.CHAT, this.getClass().getSimpleName());
    }
}
