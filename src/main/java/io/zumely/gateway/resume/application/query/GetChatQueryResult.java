package io.zumely.gateway.resume.application.query;

import io.zumely.gateway.resume.application.command.Message;

import java.util.List;

public record GetChatQueryResult(String chatId, List<Message<String>> messages) implements QueryResult {
}