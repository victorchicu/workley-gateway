package ai.workley.gateway.features.chat.domain.event;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;

import java.util.Map;

public record EmbeddingSaved(String actor, String text, Map<String, Object> metadata) implements DomainEvent {
}
