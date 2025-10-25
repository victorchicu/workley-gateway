package ai.workley.gateway.chat.application.result;

import ai.workley.gateway.chat.domain.model.Message;

import java.util.Objects;

public record CreateChatResult(String chatId, Message<String> message) implements Result {
    public CreateChatResult(String chatId, Message<String> message) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.message = Objects.requireNonNull(message, "prompt must not be null");
    }

    public static CreateChatResult response(String chatId, Message<String> message) {
        return new CreateChatResult(chatId, message);
    }
}
