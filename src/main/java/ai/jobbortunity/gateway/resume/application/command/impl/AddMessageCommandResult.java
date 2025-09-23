package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.CommandResult;
import ai.jobbortunity.gateway.resume.application.command.Message;

public record AddMessageCommandResult(String chatId, Message<String> message) implements CommandResult {

    public static AddMessageCommandResult response(String chatId, Message<String> message) {
        return new AddMessageCommandResult(chatId, message);
    }
}
