package ai.workley.gateway.chat.domain.event;

public record ReplyGenerated(String actor, String chatId, String prompt) implements DomainEvent {

}