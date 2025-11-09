package ai.workley.gateway.chat.infrastructure.eventbus;

import ai.workley.gateway.chat.domain.events.DomainEvent;

public interface EventBus {

    <T extends DomainEvent> void publishEvent(T event);
}
