package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;

import java.util.Objects;

public record CreateChatPayload(String chatId, Message<String> message) implements Payload {
    public CreateChatPayload(String chatId, Message<String> message) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.message = Objects.requireNonNull(message, "reply must not be null");
    }

    public static CreateChatPayload create(String chatId, Message<String> message) {
        return new CreateChatPayload(chatId, message);
    }
}
