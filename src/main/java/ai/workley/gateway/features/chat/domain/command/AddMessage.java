package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.domain.command.Command;

public record AddMessage(String chatId, Message<String> message) implements Command {
}
