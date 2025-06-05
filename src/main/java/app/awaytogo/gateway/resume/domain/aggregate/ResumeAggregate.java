package app.awaytogo.gateway.resume.domain.aggregate;

import app.awaytogo.gateway.resume.domain.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.domain.enums.ResumeState;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.domain.event.ResumeCreationInitiated;
import app.awaytogo.gateway.resume.domain.exception.DomainException;

import java.time.Instant;
import java.util.List;

public class ResumeAggregate implements AggregateRoot {
    private String id;
    private String resumeId;
    private ResumeState resumeState;
    private Long version;

    public static ResumeAggregate fromEvents(List<DomainEvent> events) {
        ResumeAggregate aggregate = new ResumeAggregate();
        events.forEach(aggregate::apply);
        return aggregate;
    }

    public String getId() {
        return id;
    }

    public ResumeAggregate setId(String id) {
        this.id = id;
        return this;
    }

    public String getResumeId() {
        return resumeId;
    }

    public ResumeAggregate setResumeId(String resumeId) {
        this.resumeId = resumeId;
        return this;
    }

    public ResumeState getResumeState() {
        return resumeState;
    }

    public ResumeAggregate setResumeState(ResumeState resumeState) {
        this.resumeState = resumeState;
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
                this.resumeState = ResumeState.INITIATED;
            }
            default -> {
                this.resumeState = ResumeState.UNDEFINED;
            }
        }
    }

    public List<DomainEvent> handle(CreateResumeCommand command) {
        if (this.id != null) {
            throw new DomainException("Resume already exists");
        }
        return List.of(new ResumeCreationInitiated(
                command.getResumeId(),
                command.getUserId(),
                command.getLinkedinUrl(),
                Instant.now()
        ));
    }
}