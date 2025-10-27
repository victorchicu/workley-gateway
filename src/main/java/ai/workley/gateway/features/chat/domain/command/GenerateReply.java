package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.domain.command.Command;

public record GenerateReply(String chatId, Message<String> prompt) implements Command {
}
