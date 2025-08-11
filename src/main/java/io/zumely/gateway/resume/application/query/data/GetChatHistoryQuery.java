package io.zumely.gateway.resume.application.query.data;

import io.zumely.gateway.resume.application.query.Query;

public record GetChatHistoryQuery(String chatId) implements Query {
}
