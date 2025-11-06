package ai.workley.gateway.chat.domain.aggregations;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.events.ChatCreated;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;

import java.util.*;

public class ChatAggregate {
    private final String id;
    private final Set<String> participants;
    private final long version;
    private final boolean initialized;

    private ChatAggregate(String id, Set<String> participants, long version, boolean initialized) {
        this.id = id;
        this.participants = Collections.unmodifiableSet(new LinkedHashSet<>(participants));
        this.version = version;
        this.initialized = initialized;
    }

    public static <T extends DomainEvent> ChatAggregate rehydrate(List<EventDocument<T>> history) {
        ChatAggregate aggregate = new ChatAggregate(null, Set.of(), -1L, false);
        for (EventDocument<?> entry : history) {
            aggregate = aggregate.apply(entry);
        }
        if (!aggregate.initialized) {
            throw new IllegalStateException("Chat aggregate not found");
        }
        return aggregate;
    }

    private ChatAggregate apply(EventDocument<?> entry) {
        DomainEvent event = entry.getEventData();
        long newVersion = entry.getVersion() != null ? entry.getVersion() : version + 1;

        if (event instanceof ChatCreated chatCreated) {
            Set<String> participants = new LinkedHashSet<>();
            participants.add(chatCreated.actor());
            return new ChatAggregate(chatCreated.chatId(), participants, newVersion, true);
        }

        if (!initialized) {
            return this;
        }

        if (event instanceof MessageAdded messageAdded) {
            Set<String> updated = new LinkedHashSet<>(participants);
            updated.add(messageAdded.message().ownedBy());
            return new ChatAggregate(id, updated, newVersion, true);
        }

        return new ChatAggregate(id, participants, newVersion, initialized);
    }

    public AggregateCommit<MessageAdded> addMessage(String actor, String messageId, String content) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(messageId, "messageId must not be null");
        Objects.requireNonNull(content, "content must not be null");

        if (!participants.contains(actor)) {
            throw new IllegalStateException("Actor is not part of this chat");
        }

        Message<String> message = Message.create(messageId, id, actor, content);

        return new AggregateCommit<>(new MessageAdded(actor, id, message), version);
    }

    public String id() {
        return id;
    }

    public long version() {
        return version;
    }

    public Set<String> participants() {
        return participants;
    }
}
