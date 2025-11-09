package ai.workley.gateway.chat.domain.events;

public record ReplyFailed(String actor, String chatId, String failure) implements DomainEvent {

}
