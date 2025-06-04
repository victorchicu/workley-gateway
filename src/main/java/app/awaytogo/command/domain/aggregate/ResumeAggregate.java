package app.awaytogo.command.domain.aggregate;

import app.awaytogo.command.domain.command.CreateResumeCommand;
import app.awaytogo.command.domain.enums.ResumeState;
import app.awaytogo.command.domain.event.DomainEvent;
import app.awaytogo.command.domain.event.ResumeCreationInitiated;
import app.awaytogo.command.domain.exception.DomainException;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public ResumeState getResumeState() {
        return resumeState;
    }

    public void setResumeState(ResumeState resumeState) {
        this.resumeState = resumeState;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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
        if (this.resumeId != null) {
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