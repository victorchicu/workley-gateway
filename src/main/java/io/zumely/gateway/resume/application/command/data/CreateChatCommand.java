package io.zumely.gateway.resume.application.command.data;

import io.zumely.gateway.resume.application.command.Command;

public record CreateChatCommand(String prompt) implements Command {
}