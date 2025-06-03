package app.awaytogo.gateway.resume.domain;

import java.time.Instant;
import java.util.UUID;

// It carries common information like event ID, timestamp, and aggregate ID.
public interface DomainEvent {
    UUID getEventId();       // Unique ID for this specific event instance

    String getAggregateId(); // ID of the aggregate this event belongs to

    Instant getOccurredOn(); // Timestamp when the event occurred

    String getEventType();   // String representation of the event's type (e.g., class name)

    long getSequenceNumber(); // Sequence number within the aggregate's event stream

    void setSequenceNumber(long sequenceNumber); // Setter for the store to assign
}