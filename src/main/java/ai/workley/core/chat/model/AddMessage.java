package ai.workley.core.chat.model;

public record AddMessage(String chatId, Message<? extends Content> message) implements Command {
}
