package io.zumely.gateway.resume.application.query.data;

import io.zumely.gateway.resume.application.command.data.Message;

import java.util.List;

public record GetChatHistoryQueryResult(String chatId, List<Message<String>> messages) implements QueryResult {
}