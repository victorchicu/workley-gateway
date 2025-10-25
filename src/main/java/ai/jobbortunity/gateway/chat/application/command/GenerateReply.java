package ai.jobbortunity.gateway.chat.application.command;

import ai.jobbortunity.gateway.chat.domain.model.Message;

public record GenerateReply(String chatId, Message<String> prompt) implements Command {
}
