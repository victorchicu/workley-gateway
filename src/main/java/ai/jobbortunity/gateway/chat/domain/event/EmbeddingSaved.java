package ai.jobbortunity.gateway.chat.domain.event;

import java.util.Map;

public record EmbeddingSaved(String actor, String text, Map<String, Object> metadata) implements DomainEvent {
}
