package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.command.Message;

public record IdentifyIntentCommand(String chatId, Message<String> prompt) implements Command {

}