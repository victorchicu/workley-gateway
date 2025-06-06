package app.awaytogo.gateway.resume.domain.aggregate;

import app.awaytogo.gateway.resume.domain.command.CreateResumeCommand;
import app.awaytogo.gateway.resume.domain.enums.ProcessingState;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.domain.event.ResumeCreationInitiated;
import app.awaytogo.gateway.resume.domain.exception.DomainException;

import java.time.Instant;
import java.util.List;

public class ResumeAggregate implements AggregateRoot {
    private String resumeId;
    private String aggregateId;
    private ProcessingState processingState;
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

    public String getAggregateId() {
        return aggregateId;
    }

    public ResumeAggregate setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
        return this;
    }

    public ProcessingState getResumeState() {
        return processingState;
    }

    public ResumeAggregate setResumeState(ProcessingState processingState) {
        this.processingState = processingState;
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
                this.processingState = ProcessingState.INITIATED;
            }
            default -> this.processingState = ProcessingState.UNKNOWN;
        }
    }

    public List<DomainEvent> handle(CreateResumeCommand command) {
        if (this.aggregateId != null) {
            throw new DomainException("You’ve already completed this step.");
        }
        return List.of(
                new ResumeCreationInitiated(
                        command.getResumeId(),
                        command.getPrincipal().getName(),
                        command.getSource(),
                        Instant.now()
                ));
    }
}