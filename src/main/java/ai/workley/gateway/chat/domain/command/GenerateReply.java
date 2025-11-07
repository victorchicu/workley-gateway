package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;

public record GenerateReply(String chatId, Message<String> message) implements Command {
}
