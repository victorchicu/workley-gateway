package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.app.command.results.Output;

import java.util.Objects;

public record CreateChatOutput(String chatId, Message<String> message) implements Output {
    public CreateChatOutput(String chatId, Message<String> message) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.message = Objects.requireNonNull(message, "prompt must not be null");
    }

    public static CreateChatOutput response(String chatId, Message<String> message) {
        return new CreateChatOutput(chatId, message);
    }
}
