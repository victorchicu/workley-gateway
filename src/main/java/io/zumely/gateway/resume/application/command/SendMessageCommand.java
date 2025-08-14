package io.zumely.gateway.resume.application.command;

public record SendMessageCommand(String chatId, Message<String> message) implements Command {
}