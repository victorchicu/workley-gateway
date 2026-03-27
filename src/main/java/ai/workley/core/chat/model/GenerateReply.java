package ai.workley.core.chat.model;

public record GenerateReply(String chatId, Message<? extends Content> message) implements Command {
}
