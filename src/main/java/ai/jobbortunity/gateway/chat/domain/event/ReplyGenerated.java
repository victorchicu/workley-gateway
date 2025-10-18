package ai.jobbortunity.gateway.chat.domain.event;

public record ReplyGenerated(String actor, String chatId, String reply) implements DomainEvent {
}
