package ai.jobbortunity.gateway.chat.domain.event;

public record ChatCreated(String actor, String chatId, String prompt) implements DomainEvent {
}
