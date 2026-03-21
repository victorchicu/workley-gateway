package ai.workley.gateway.chat.model;

import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Content;

public record GenerateReply(String chatId, Message<? extends Content> message) implements Command {
}
