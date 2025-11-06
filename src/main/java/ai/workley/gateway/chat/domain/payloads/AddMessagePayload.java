package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;

public record AddMessagePayload(String chatId, Message<String> message) implements Payload {

    public static AddMessagePayload create(String chatId, Message<String> message) {
        return new AddMessagePayload(chatId, message);
    }
}
