package ai.workley.gateway.chat.domain.events;

public record Aggregation(String id, String type, String event, Long version) {
}
