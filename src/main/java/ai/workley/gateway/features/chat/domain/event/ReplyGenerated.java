package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;

public record ReplyGenerated(String actor, String chatId, String prompt) implements DomainEvent {

}