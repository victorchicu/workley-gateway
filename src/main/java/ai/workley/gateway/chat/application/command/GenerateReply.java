package ai.workley.gateway.chat.application.command;

import ai.workley.gateway.chat.domain.model.Message;

public record GenerateReply(String chatId, Message<String> prompt) implements Command {
}
