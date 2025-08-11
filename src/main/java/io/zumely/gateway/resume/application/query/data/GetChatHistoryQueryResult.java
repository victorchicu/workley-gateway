package io.zumely.gateway.resume.application.query.data;

import java.util.List;

public record GetChatHistoryQueryResult(String chatId, List<Message<String>> data) implements QueryResult {
}
