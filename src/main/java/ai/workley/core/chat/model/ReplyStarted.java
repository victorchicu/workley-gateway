package ai.workley.core.chat.model;

public record ReplyStarted(String actor, String chatId, Message<? extends Content> message) implements DomainEvent {

}
