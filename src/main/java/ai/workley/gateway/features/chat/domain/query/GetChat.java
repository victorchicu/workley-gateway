package ai.workley.gateway.features.chat.domain.query;

import ai.workley.gateway.features.shared.domain.query.Query;

public record GetChat(String chatId) implements Query {
}
