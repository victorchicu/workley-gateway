package io.zumely.gateway.resume.application.command.data;

import io.zumely.gateway.resume.application.command.Command;

public record SendMessageCommand(String chatId, Message<String> message) implements Command {
}