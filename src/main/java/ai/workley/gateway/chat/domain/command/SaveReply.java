package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.TextContent;

public record SaveReply(String chatId, Message<TextContent> reply) implements Command {
}
