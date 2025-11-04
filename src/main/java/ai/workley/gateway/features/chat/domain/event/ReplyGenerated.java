package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.infra.prompt.ClassificationResult;
import ai.workley.gateway.features.shared.domain.aggregations.AggregateTypes;
import ai.workley.gateway.features.shared.domain.event.DomainEvent;

public record ReplyGenerated(String actor, String chatId, Message<String> prompt, ClassificationResult classification) implements DomainEvent {

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
