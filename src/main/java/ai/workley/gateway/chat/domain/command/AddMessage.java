package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;

public record AddMessage(String chatId, Message<String> message) implements Command {
}
