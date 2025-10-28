package ai.workley.gateway.features.chat.domain.query;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.app.command.results.Output;

import java.util.List;

public record GetChatOutput(String chatId, List<Message<String>> messages) implements Output {
}
