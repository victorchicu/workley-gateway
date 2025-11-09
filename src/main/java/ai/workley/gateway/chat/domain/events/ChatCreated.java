package ai.workley.gateway.chat.domain.events;

public record ChatCreated(String actor, String chatId, String prompt) implements DomainEvent {

}
