package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.Command;

public record CreateChatCommand(String prompt) implements Command {
}
