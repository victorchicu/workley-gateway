package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;

public record SaveEmbeddingCommand(String type, String reference, String text) implements Command {

}
