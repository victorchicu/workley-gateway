package ai.workley.gateway.chat.application.ports.outbound.bus;

import ai.workley.gateway.chat.domain.events.DomainEvent;

public interface EventBus {

    <T extends DomainEvent> void publishEvent(T event);
}
