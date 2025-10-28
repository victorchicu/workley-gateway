package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.shared.domain.event.DomainEvent;

public record MessageAdded(String actor, String chatId, Message<String> message) implements DomainEvent {
}
