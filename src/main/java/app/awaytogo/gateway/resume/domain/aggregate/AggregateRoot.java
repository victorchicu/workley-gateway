package app.awaytogo.gateway.resume.domain.aggregate;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;

public interface AggregateRoot {

    void apply(DomainEvent event);
}
