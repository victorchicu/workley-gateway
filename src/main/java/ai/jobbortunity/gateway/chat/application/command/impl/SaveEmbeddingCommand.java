package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;

public record SaveEmbeddingCommand(String text) implements Command {

}
