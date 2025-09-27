package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.service.Intent;

public record GenerateReplyCommand(String chatId, Intent intent, Message<String> prompt) implements Command {
}
