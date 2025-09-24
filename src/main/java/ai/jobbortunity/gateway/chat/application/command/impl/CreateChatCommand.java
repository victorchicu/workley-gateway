package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;

public record CreateChatCommand(String prompt) implements Command {
}
