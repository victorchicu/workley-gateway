package ai.workley.core.chat.service;

import ai.workley.core.chat.model.DomainEvent;

public interface EventBus {

    <T extends DomainEvent> void publishEvent(T event);
}
