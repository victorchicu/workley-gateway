package app.awaytogo.gateway.resume.domain.aggregate;

import app.awaytogo.gateway.resume.domain.command.impl.SubmitLinkedInPublicProfileCommand;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.domain.event.ResumeCreationInitiated;
import app.awaytogo.gateway.resume.domain.exception.DomainException;

import java.time.Instant;
import java.util.List;

public class ResumeAggregate implements AggregateRoot {
    private String resumeId;
    private Long version;

    public static ResumeAggregate fromEvents(List<DomainEvent> events) {
        ResumeAggregate aggregate = new ResumeAggregate();
        events.forEach(aggregate::apply);
        return aggregate;
    }

    public String getResumeId() {
        return resumeId;
    }

    public ResumeAggregate setResumeId(String resumeId) {
        this.resumeId = resumeId;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public ResumeAggregate setVersion(Long version) {
        this.version = version;
        return this;
    }

    public void apply(DomainEvent event) {
        switch (event) {
            case ResumeCreationInitiated e -> {
                this.resumeId = e.getResumeId();
            }
            default -> throw new IllegalStateException("Unexpected value: " + event);
        }
    }

    public List<DomainEvent> handle(SubmitLinkedInPublicProfileCommand command) {
        if (this.resumeId != null) {
            throw new DomainException("LinkedIn public profile has already been submitted");
        }
        return List.of(new ResumeCreationInitiated(command.getResumeId(), Instant.now()));
    }

    public enum State {
        UNKNOWN,
        INITIATED,
        PROFILE_FETCHING,
        PROFILE_FETCHED,
        PROFILE_FETCH_FAILED,
    }
}
