package ai.workley.gateway.chat.domain.command;

import ai.workley.gateway.chat.domain.Message;

public record SaveReply(String chatId, Message<String> reply) implements Command {
}
