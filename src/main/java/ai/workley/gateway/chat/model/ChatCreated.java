package ai.workley.gateway.chat.model;

public record ChatCreated(String actor, String chatId, String prompt) implements DomainEvent {

}
