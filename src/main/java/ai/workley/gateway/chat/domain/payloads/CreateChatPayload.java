package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.ReplyChunk;

import java.util.Objects;

public record CreateChatPayload(String chatId, Message<ReplyChunk> message) implements Payload {
    public CreateChatPayload(String chatId, Message<ReplyChunk> message) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public static CreateChatPayload ack(String chatId, Message<ReplyChunk> message) {
        return new CreateChatPayload(chatId, message);
    }
}
