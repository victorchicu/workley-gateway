package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.ReplyChunk;

public record SaveReply(String chatId, Message<ReplyChunk> reply) implements Command {
}
