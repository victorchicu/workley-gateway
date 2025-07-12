package ai.zumely.gateway.resume.domain.aggregate;

import ai.zumely.gateway.resume.domain.event.DomainEvent;

public interface AggregateRoot {

    void apply(DomainEvent event);
}
