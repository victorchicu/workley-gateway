package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;

public record AddMessagePayload(String chatId, Message<? extends Content> message) implements Payload {

    public static AddMessagePayload ack(String chatId, Message<? extends Content> message) {
        return new AddMessagePayload(chatId, message);
    }
}
