package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.Command;
import ai.jobbortunity.gateway.resume.application.command.Message;

public record SaveEmbeddingCommand(Message<String> message) implements Command {

}
