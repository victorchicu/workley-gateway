package ai.workley.gateway.chat.application.result;

import ai.workley.gateway.chat.domain.model.Message;

public record AddMessageResult(String chatId, Message<String> message) implements Result {

    public static AddMessageResult response(String chatId, Message<String> message) {
        return new AddMessageResult(chatId, message);
    }
}
