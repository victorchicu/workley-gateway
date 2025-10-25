package ai.workley.gateway.chat.application.result;

import ai.workley.gateway.chat.domain.model.Message;

import java.util.List;

public record GetChatResult(String chatId, List<Message<String>> messages) implements Result {
}
