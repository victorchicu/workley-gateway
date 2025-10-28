package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.app.command.results.Output;

public record AddMessageOutput(String chatId, Message<String> message) implements Output {

    public static AddMessageOutput response(String chatId, Message<String> message) {
        return new AddMessageOutput(chatId, message);
    }
}
