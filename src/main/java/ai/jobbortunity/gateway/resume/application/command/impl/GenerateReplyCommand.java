package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.Command;
import ai.jobbortunity.gateway.resume.application.command.Message;

public record GenerateReplyCommand(String chatId, Message<String> prompt) implements Command {
}
