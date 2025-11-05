package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassification;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;

public record ReplyGenerated(String actor, String chatId, Message<String> prompt, IntentClassification classification) implements DomainEvent {

    @Override
    public String eventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String aggregateId() {
        return prompt.id();
    }

    @Override
    public String aggregateType() {
        return AggregateTypes.CHAT;
    }
}
