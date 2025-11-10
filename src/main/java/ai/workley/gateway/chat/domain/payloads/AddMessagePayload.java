package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;

public record AddMessagePayload(String chatId, Message<TextContent> message) implements Payload {

    public static AddMessagePayload ack(String chatId, Message<TextContent> message) {
        return new AddMessagePayload(chatId, message);
    }
}
