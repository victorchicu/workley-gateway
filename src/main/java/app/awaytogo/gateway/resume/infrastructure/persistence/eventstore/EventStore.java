package app.awaytogo.gateway.resume.infrastructure.persistence.eventstore;

import app.awaytogo.gateway.resume.domain.DomainEvent;

import java.util.List;

public interface EventStore {
    void saveEvents(String resumeId, List<DomainEvent> newEvents, int version);
}
