package ai.workley.core.chat.model;

public record ReplyFailed(String actor, String chatId, String failure) implements DomainEvent {

}
