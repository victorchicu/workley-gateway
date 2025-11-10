package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;

public record GenerateReply(String chatId, Message<TextContent> message) implements Command {
}
