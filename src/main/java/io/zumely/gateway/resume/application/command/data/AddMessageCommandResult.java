package io.zumely.gateway.resume.application.command.data;

public record AddMessageCommandResult(String chatId, Message<String> message) implements CommandResult {

    public static AddMessageCommandResult response(String chatId, Message<String> message) {
        return new AddMessageCommandResult(chatId, message);
    }
}