package ai.workley.gateway.chat.application.command;

import ai.workley.gateway.chat.domain.model.Message;

public record AddMessage(String chatId, Message<String> message) implements Command {
}
