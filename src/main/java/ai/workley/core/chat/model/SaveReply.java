package ai.workley.core.chat.model;

public record SaveReply(String chatId, Message<ReplyChunk> reply) implements Command {
}
