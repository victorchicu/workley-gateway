package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.command.Message;

public record SaveEmbeddingCommand(String chatId, Message<String> message) implements Command {

}
