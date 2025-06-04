package app.awaytogo.command.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant getTimestamp();
}
