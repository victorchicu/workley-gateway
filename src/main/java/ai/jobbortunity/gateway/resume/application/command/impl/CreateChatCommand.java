package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.Command;

public record CreateChatCommand(String prompt) implements Command {
}
