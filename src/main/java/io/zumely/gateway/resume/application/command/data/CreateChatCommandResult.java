package io.zumely.gateway.resume.application.command.data;

import io.zumely.gateway.resume.application.event.ApplicationEvent;

import java.util.Objects;

public record CreateChatCommandResult(String chatId, Prompt prompt) implements CommandResult {
    public CreateChatCommandResult(String chatId, Prompt prompt) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.prompt = Objects.requireNonNull(prompt, "prompt must not be null");
    }

    public static <T extends ApplicationEvent> CreateChatCommandResult response(String chatId, Prompt prompt) {
        return new CreateChatCommandResult(
                chatId,
                prompt
        );
    }
}
