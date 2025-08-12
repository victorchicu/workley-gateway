package io.zumely.gateway.resume.application.command.data;

public record SendMessageCommandResult(String chatId, Message<String> message) implements CommandResult {

    public static SendMessageCommandResult response(String chatId, Message<String> message) {
        return new SendMessageCommandResult(chatId, message);
    }
}