package io.zumely.gateway.resume.application.command;

public record AddMessageCommand(String chatId, Message<String> message) implements Command {
}