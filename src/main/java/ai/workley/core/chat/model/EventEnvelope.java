package ai.workley.core.chat.model;

public record EventEnvelope<T extends DomainEvent>(
        String aggregateType,
        String aggregateId,
        Long version,
        String eventType,
        T eventData
) {
}
