package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.CommandResult;
import io.zumely.gateway.resume.application.command.Message;

public record AddMessageCommandResult(String chatId, Message<String> message) implements CommandResult {

    public static AddMessageCommandResult response(String chatId, Message<String> message) {
        return new AddMessageCommandResult(chatId, message);
    }
}
