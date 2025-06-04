package app.awaytogo.command.domain.aggregate;

import app.awaytogo.command.domain.event.DomainEvent;

public interface AggregateRoot {

    void apply(DomainEvent event);
}
