package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import com.github.f4b6a3.tsid.TsidCreator;

public record ReplyFailed(String actor, String chatId, String failure) implements DomainEvent {

    @Override
    public Aggregation aggregation() {
        return new Aggregation(chatId, AggregateTypes.CHAT, this.getClass().getSimpleName(), TsidCreator.getTsid().toLong());
    }
}
