package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;

public record AddMessagePayload(String chatId, Message<? extends Content> message) implements Payload {

    public static AddMessagePayload ack(String chatId, Message<? extends Content> message) {
        return new AddMessagePayload(chatId, message);
    }
}
