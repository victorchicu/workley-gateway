package ai.workley.gateway.chat.model;

public record ReplyFailed(String actor, String chatId, String failure) implements DomainEvent {

}
