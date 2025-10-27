package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.shared.domain.command.Command;

public record CreateChat(String prompt) implements Command {
}
