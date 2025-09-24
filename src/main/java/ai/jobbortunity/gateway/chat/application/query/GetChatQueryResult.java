package ai.jobbortunity.gateway.chat.application.query;

import ai.jobbortunity.gateway.chat.application.command.Message;

import java.util.List;

public record GetChatQueryResult(String chatId, List<Message<String>> messages) implements QueryResult {
}
