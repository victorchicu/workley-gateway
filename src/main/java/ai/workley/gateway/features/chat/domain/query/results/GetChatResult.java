package ai.workley.gateway.features.chat.domain.query.results;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.app.command.results.Result;

import java.util.List;

public record GetChatResult(String chatId, List<Message<String>> messages) implements Result {
}
