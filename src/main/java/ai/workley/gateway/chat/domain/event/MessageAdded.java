package ai.workley.gateway.chat.domain.event;

import ai.workley.gateway.chat.domain.model.Message;

public record MessageAdded(String actor, String chatId, Message<String> message) implements DomainEvent {
}
