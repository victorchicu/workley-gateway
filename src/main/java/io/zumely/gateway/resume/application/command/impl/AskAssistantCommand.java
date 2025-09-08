package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.Command;

public record AskAssistantCommand(String prompt, String chatId) implements Command {
}
