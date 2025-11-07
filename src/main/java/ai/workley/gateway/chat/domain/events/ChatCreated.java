package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record ChatCreated(String actor, String chatId, String prompt) implements DomainEvent {

    @Override
    public Aggregation aggregation() {
        return new Aggregation(chatId, AggregateTypes.CHAT, this.getClass().getSimpleName());
    }
}
