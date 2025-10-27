package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.shared.domain.command.Command;

public record SaveEmbedding(String text) implements Command {

}
