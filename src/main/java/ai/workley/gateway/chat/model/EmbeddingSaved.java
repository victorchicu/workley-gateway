package ai.workley.gateway.chat.model;

import java.util.Map;

public record EmbeddingSaved(String actor, String text, Map<String, Object> metadata) implements DomainEvent {

}
