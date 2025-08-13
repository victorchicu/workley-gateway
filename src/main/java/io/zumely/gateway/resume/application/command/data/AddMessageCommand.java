package io.zumely.gateway.resume.application.command.data;

import io.zumely.gateway.resume.application.command.Command;

public record AddMessageCommand(String chatId, Message<String> message) implements Command {
}