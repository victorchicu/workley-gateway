package ai.workley.core.chat.model;

public record ReplySaved(String actor, String chatId, Message<ReplyChunk> message) implements DomainEvent {

}
