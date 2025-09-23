package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.Message;

public record SaveEmbeddingCommand(Message<String> message) implements Command {

}
