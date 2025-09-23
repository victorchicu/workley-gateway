package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.Command;

public record GenerateReplyCommand(String prompt, String chatId) implements Command {
}
