package ai.jobbortunity.gateway.chat.domain.event;

public record PromptSubmitted(String actor, String chatId, String prompt) implements DomainEvent {

}