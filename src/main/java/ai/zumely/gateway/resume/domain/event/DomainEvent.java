package ai.zumely.gateway.resume.domain.event;

import java.time.Instant;

public interface DomainEvent {

    Instant getCreatedOn();
}
