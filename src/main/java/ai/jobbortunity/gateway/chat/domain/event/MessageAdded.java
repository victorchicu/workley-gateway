package ai.jobbortunity.gateway.chat.domain.event;

import ai.jobbortunity.gateway.chat.domain.model.Message;

public record MessageAdded(String actor, String chatId, Message<String> message) implements DomainEvent {
}
