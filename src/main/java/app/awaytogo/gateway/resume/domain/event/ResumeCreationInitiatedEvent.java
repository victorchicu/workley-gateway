package app.awaytogo.gateway.resume.domain.event;

import app.awaytogo.gateway.resume.domain.DomainEvent;
import app.awaytogo.gateway.resume.domain.ResumeId;
import app.awaytogo.gateway.resume.domain.ResumeStatus;
import app.awaytogo.gateway.resume.domain.UserId;

import java.time.Instant;
import java.util.UUID;

public class ResumeCreationInitiatedEvent implements DomainEvent {

    private final UUID eventId;
    private final String aggregateId; // ResumeId as String
    private final Instant occurredOn;
    private final String userId; // UserId as String
    private final String linkedinUrl;
    private final String status; // e.g., PENDING_PROFILE_FETCH
    private long sequenceNumber;


    public ResumeCreationInitiatedEvent(ResumeId resumeId, UserId userId, String linkedinUrl, ResumeStatus initialStatus) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = resumeId.value();
        this.userId = userId.value();
        this.linkedinUrl = linkedinUrl;
        this.status = initialStatus.name();
        this.occurredOn = Instant.now();
    }

    // Private constructor for deserialization if needed
    private ResumeCreationInitiatedEvent(UUID eventId, String aggregateId, Instant occurredOn, String userId, String linkedinUrl, String status, long sequenceNumber) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.occurredOn = occurredOn;
        this.userId = userId;
        this.linkedinUrl = linkedinUrl;
        this.status = status;
        this.sequenceNumber = sequenceNumber;
    }


    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    // Getters for event-specific data
    public String getUserId() {
        return userId;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getStatus() {
        return status;
    }
}
