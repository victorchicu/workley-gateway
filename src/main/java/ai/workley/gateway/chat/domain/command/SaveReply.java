package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.ReplyChunk;

public record SaveReply(String chatId, Message<ReplyChunk> reply) implements Command {
}
