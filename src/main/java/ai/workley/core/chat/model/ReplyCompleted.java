package ai.workley.core.chat.model;

public record ReplyCompleted(String actor, String chatId, Message<? extends Content> message) implements DomainEvent {

}
