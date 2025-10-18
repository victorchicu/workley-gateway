package ai.jobbortunity.gateway.chat.application.result;

import ai.jobbortunity.gateway.chat.domain.model.Message;

import java.util.List;

public record GetChatResult(String chatId, List<Message<String>> messages) implements Result {
}
