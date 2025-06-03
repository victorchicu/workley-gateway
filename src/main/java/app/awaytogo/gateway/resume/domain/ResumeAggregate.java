package app.awaytogo.gateway.resume.domain;

import app.awaytogo.gateway.resume.domain.event.ResumeCreationInitiatedEvent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResumeAggregate {
    private ResumeId id;
    private UserId userId;
    private String linkedinUrl;
    private ResumeStatus status;
    private long version = -1; // Version of the aggregate (last applied event sequence number)

    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    // Private constructor for reconstitution
    private ResumeAggregate(ResumeId id) {
        this.id = id;
    }

    // Factory method for creation
    public static ResumeAggregate create(ResumeId resumeId, UserId userId, String linkedinUrl) {
        ResumeAggregate resume = new ResumeAggregate(resumeId);
        ResumeCreationInitiatedEvent event = new ResumeCreationInitiatedEvent(resumeId, userId, linkedinUrl, ResumeStatus.PENDING_PROFILE_FETCH);
        resume.applyNewEvent(event); // Internal method to apply and add to uncommittedEvents
        return resume;
    }

    // Method to apply a new event and add it to uncommitted list
    private void applyNewEvent(DomainEvent event) {
        // Here, you would typically call a specific 'apply(SpecificEvent event)' method
        // using pattern matching or if-else to update the aggregate's state based on the event type.
        // For example:
        if (event instanceof ResumeCreationInitiatedEvent rcie) {
            this.id = new ResumeId(rcie.getAggregateId()); // redundant if constructor sets it
            this.userId = new UserId(rcie.getUserId());
            this.linkedinUrl = rcie.getLinkedinUrl();
            this.status = ResumeStatus.valueOf(rcie.getStatus());
        }
        // ... other event types

        this.uncommittedEvents.add(event);
        // The version of the aggregate isn't incremented until events are committed
        // Or, if events carry their own sequence number, that's used.
    }

    // Method to reconstitute from events
    public static ResumeAggregate reconstitute(ResumeId id, List<DomainEvent> history) {
        ResumeAggregate aggregate = new ResumeAggregate(id);
        history.forEach(aggregate::applyCommittedEvent);
        return aggregate;
    }

    // Applies a committed event (during reconstitution or after saving) without adding to uncommittedEvents
    private void applyCommittedEvent(DomainEvent event) {
        // Similar logic to applyNewEvent for state changes
        if (event instanceof ResumeCreationInitiatedEvent rcie) {
            this.id = new ResumeId(rcie.getAggregateId());
            this.userId = new UserId(rcie.getUserId());
            this.linkedinUrl = rcie.getLinkedinUrl();
            this.status = ResumeStatus.valueOf(rcie.getStatus());
        }
        // ... other event types

        this.version = event.getSequenceNumber(); // Update version from the event's sequence number
    }


    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }

    public void markEventsAsCommitted() {
        if (!uncommittedEvents.isEmpty()) {
            this.version = uncommittedEvents.getLast().getSequenceNumber();
        }
        uncommittedEvents.clear();
    }

    public ResumeId getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }
}
