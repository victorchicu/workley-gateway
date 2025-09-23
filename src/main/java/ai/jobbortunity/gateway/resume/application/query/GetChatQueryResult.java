package ai.jobbortunity.gateway.resume.application.query;

import ai.jobbortunity.gateway.resume.application.command.Message;

import java.util.List;

public record GetChatQueryResult(String chatId, List<Message<String>> messages) implements QueryResult {
}
