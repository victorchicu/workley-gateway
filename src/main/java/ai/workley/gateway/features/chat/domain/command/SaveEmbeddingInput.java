package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.shared.domain.command.Command;

public record SaveEmbeddingInput(String text) implements Command {

}
