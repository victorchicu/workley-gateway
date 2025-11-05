package ai.workley.gateway.chat.domain.events;

public interface DomainEvent {

    String eventType();

    String aggregateId();

    String aggregateType();
}
