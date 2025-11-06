package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import com.github.f4b6a3.tsid.TsidCreator;

public record ReplyInitiated(String actor, String chatId, Message<String> prompt) implements DomainEvent {

    @Override
    public Aggregation aggregation() {
        return new Aggregation(chatId, AggregateTypes.CHAT, this.getClass().getSimpleName());
    }
}
