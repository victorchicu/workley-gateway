package ai.workley.gateway.chat.domain.aggregations;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.events.ChatCreated;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;

import java.util.*;

public record ChatAggregate(String id, Set<String> participants, long version) {
    public ChatAggregate(String id, Set<String> participants, long version) {
        this.id = id;
        this.participants = Collections.unmodifiableSet(new LinkedHashSet<>(participants));
        this.version = version;
    }

    public static <T extends DomainEvent> ChatAggregate rehydrate(List<EventDocument<T>> history) {
        ChatAggregate aggregate = new ChatAggregate(null, Set.of(), -1L);
        for (EventDocument<T> entry : history) {
            aggregate = aggregate.apply(entry);
        }
        return aggregate;
    }

    public AggregateCommit<MessageAdded> appendMessage(String actor, String messageId, String content) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        Objects.requireNonNull(content, "content must not be null");

        if (!participants.contains(actor)) {
            throw new IllegalStateException("Actor is not part of this chat");
        }

        Message<String> message = Message.create(messageId, id, actor, content);

        return new AggregateCommit<>(new MessageAdded(actor, id, message), version);
    }

    private <T extends DomainEvent> ChatAggregate apply(EventDocument<T> entry) {
        DomainEvent event = entry.getEventData();
        long newVersion = entry.getVersion() != null ? entry.getVersion() : version + 1;

        if (event instanceof ChatCreated chatCreated) {
            Set<String> participants = new LinkedHashSet<>();
            participants.add(chatCreated.actor());
            return new ChatAggregate(chatCreated.chatId(), participants, newVersion);
        }

        if (event instanceof MessageAdded messageAdded) {
            Set<String> participants = new LinkedHashSet<>(this.participants);
            participants.add(messageAdded.message().ownedBy());
            return new ChatAggregate(id, participants, newVersion);
        }

        return new ChatAggregate(id, participants, newVersion);
    }
}
