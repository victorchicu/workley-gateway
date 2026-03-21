package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.model.DomainEvent;

public interface EventBus {

    <T extends DomainEvent> void publishEvent(T event);
}
