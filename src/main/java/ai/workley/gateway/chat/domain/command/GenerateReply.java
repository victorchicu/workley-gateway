package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;

public record GenerateReply(String chatId, Message<? extends Content> message) implements Command {
}
