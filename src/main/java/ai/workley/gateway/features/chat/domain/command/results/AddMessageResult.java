package ai.workley.gateway.features.chat.domain.command.results;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.app.command.results.Result;

public record AddMessageResult(String chatId, Message<String> message) implements Result {

    public static AddMessageResult response(String chatId, Message<String> message) {
        return new AddMessageResult(chatId, message);
    }
}
