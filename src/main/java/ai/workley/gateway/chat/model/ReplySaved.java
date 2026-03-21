package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.ReplyChunk;

public record ReplySaved(String actor, String chatId, Message<ReplyChunk> message) implements DomainEvent {

}