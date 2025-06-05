package app.awaytogo.gateway.resume.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant getTimestamp();
}
