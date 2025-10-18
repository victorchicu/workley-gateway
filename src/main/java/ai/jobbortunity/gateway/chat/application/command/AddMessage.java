package ai.jobbortunity.gateway.chat.application.command;

import ai.jobbortunity.gateway.chat.domain.model.Message;

public record AddMessage(String chatId, Message<String> message) implements Command {
}
