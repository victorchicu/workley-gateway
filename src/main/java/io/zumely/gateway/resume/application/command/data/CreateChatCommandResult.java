package io.zumely.gateway.resume.application.command.data;

import java.util.Objects;

public record CreateChatCommandResult(String chatId, Message<String> message) implements CommandResult {
    public CreateChatCommandResult(String chatId, Message<String> message) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public static CreateChatCommandResult response(String chatId, Message<String> message) {
        return new CreateChatCommandResult(chatId, message);
    }
}
