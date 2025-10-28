package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;

public record ReplyCompleted(String actor, String chatId, String reply) implements DomainEvent {
}
