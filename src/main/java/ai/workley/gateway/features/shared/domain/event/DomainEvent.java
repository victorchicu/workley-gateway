package ai.workley.gateway.features.shared.domain.event;

public interface DomainEvent {

    String eventType();

    String aggregateId();

    String aggregateType();
}
