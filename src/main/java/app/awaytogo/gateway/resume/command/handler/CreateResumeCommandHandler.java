package app.awaytogo.gateway.resume.command.handler;

import app.awaytogo.gateway.resume.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.domain.DomainEvent;
import app.awaytogo.gateway.resume.domain.ResumeAggregate;
import app.awaytogo.gateway.resume.domain.ResumeId;
import app.awaytogo.gateway.resume.infrastructure.messaging.DomainEventPublisher;
import app.awaytogo.gateway.resume.infrastructure.persistence.eventstore.EventStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class CreateResumeCommandHandler {
    private final EventStore eventStore;
    private final DomainEventPublisher domainEventPublisher;
    // Optional: private final ResumeAggregateFactory resumeAggregateFactory;

    public CreateResumeCommandHandler(EventStore eventStore, DomainEventPublisher domainEventPublisher) {
        this.eventStore = eventStore;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * Handles the CreateResumeCommand.
     *
     * This method should ideally be transactional if the event store operations
     * and event publishing (especially if using an outbox pattern) need to be atomic.
     * For a simple "save then publish" approach, @Transactional might be on eventStore.saveEvents.
     *
     * @param command The command containing data to create a resume.
     */
    @Transactional
    public void handle(CreateResumeCommand command) {
        ResumeId resumeId = new ResumeId(command.resumeId());
        ResumeAggregate resume = ResumeAggregate.create(resumeId, command.principal(), command.linkedinUrl());
        List<DomainEvent> newEvents = resume.getUncommittedEvents();
        eventStore.saveEvents(resumeId.value(), newEvents, 0); // Assuming 0 for new aggregate's version
        for (DomainEvent event : newEvents) {
            domainEventPublisher.publish(event);
        }
        resume.markEventsAsCommitted();
    }
}
