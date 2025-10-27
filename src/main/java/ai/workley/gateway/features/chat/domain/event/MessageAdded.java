package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.chat.domain.Message;

public record MessageAdded(String actor, String chatId, Message<String> message) implements DomainEvent {
}
