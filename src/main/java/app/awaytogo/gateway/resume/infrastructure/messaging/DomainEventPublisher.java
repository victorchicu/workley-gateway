package app.awaytogo.gateway.resume.infrastructure.messaging;

import app.awaytogo.gateway.resume.domain.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
