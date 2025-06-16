package app.awaytogo.gateway.resume.domain.aggregate;

import app.awaytogo.gateway.resume.domain.command.impl.SubmitLinkedInPublicProfileCommand;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.domain.event.ResumeCreationInitiated;
import app.awaytogo.gateway.resume.domain.exception.DomainException;

import java.time.Instant;
import java.util.List;

public class ResumeAggregate implements AggregateRoot {
    private Long version;
    private State state;
    private String resumeId;

    public static ResumeAggregate fromEvents(List<DomainEvent> events) {
        ResumeAggregate aggregate = new ResumeAggregate();
        events.forEach(aggregate::apply);
        return aggregate;
    }

    public Long getVersion() {
        return version;
    }

    public ResumeAggregate setVersion(Long version) {
        this.version = version;
        return this;
    }

    public State getState() {
        return state;
    }

    public ResumeAggregate setState(State state) {
        this.state = state;
        return this;
    }

    public String getResumeId() {
        return resumeId;
    }

    public ResumeAggregate setResumeId(String resumeId) {
        this.resumeId = resumeId;
        return this;
    }

    public void apply(DomainEvent event) {
        switch (event) {
            case ResumeCreationInitiated e -> {
                this.resumeId = e.getResumeId();
                this.state = State.INITIATED;
            }
            default -> this.state = State.UNKNOWN;
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
