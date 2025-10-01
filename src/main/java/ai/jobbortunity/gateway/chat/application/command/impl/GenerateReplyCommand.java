package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.service.ClassificationResult;

public record GenerateReplyCommand(String chatId, ClassificationResult classificationResult, Message<String> prompt) implements Command {
}
