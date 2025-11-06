package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import ai.workley.gateway.chat.infrastructure.intent.IntentClassification;
import com.github.f4b6a3.tsid.TsidCreator;

public record ReplyGenerated(String actor, String chatId, Message<String> reply, IntentClassification classification) implements DomainEvent {

    @Override
    public Aggregation aggregation() {
        return new Aggregation(chatId, AggregateTypes.CHAT, this.getClass().getSimpleName(), TsidCreator.getTsid().toLong());
    }
}
