package ai.workley.gateway.chat.application.ports.outbound;

import ai.workley.gateway.chat.domain.events.DomainEvent;

public interface EventBus {

    <T extends DomainEvent> void publishEvent(T event);
}
