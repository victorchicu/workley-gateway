package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.CommandResult;
import io.zumely.gateway.resume.application.command.Message;

public record AddChatMessageCommandResult(String chatId, Message<String> message) implements CommandResult {

    public static AddChatMessageCommandResult response(String chatId, Message<String> message) {
        return new AddChatMessageCommandResult(chatId, message);
    }
}
