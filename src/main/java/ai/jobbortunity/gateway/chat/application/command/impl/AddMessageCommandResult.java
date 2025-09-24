package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandResult;
import ai.jobbortunity.gateway.chat.application.command.Message;

public record AddMessageCommandResult(String chatId, Message<String> message) implements CommandResult {

    public static AddMessageCommandResult response(String chatId, Message<String> message) {
        return new AddMessageCommandResult(chatId, message);
    }
}
