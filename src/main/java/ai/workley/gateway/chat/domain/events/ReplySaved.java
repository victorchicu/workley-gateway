package ai.workley.gateway.chat.domain.events;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.ReplyChunk;

public record ReplySaved(String actor, String chatId, Message<ReplyChunk> message) implements DomainEvent {

}