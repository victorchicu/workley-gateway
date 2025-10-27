package ai.workley.gateway.features.chat.domain.event;

public record ReplyCompleted(String actor, String chatId, String reply) implements DomainEvent {
}
