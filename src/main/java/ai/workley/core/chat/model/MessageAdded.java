package ai.workley.core.chat.model;

public record MessageAdded(String actor, String chatId, Message<? extends Content> message) implements DomainEvent {

}
