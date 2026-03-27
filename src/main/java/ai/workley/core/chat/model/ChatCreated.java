package ai.workley.core.chat.model;

public record ChatCreated(String actor, String chatId, String prompt) implements DomainEvent {

}
