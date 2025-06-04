package app.awaytogo.command.domain.aggregate;

import app.awaytogo.command.domain.enums.ResumeState;
import app.awaytogo.command.domain.event.DomainEvent;
import app.awaytogo.command.domain.event.ResumeCreationInitiated;

import java.util.List;

public class ResumeAggregate implements AggregateRoot {
    private String resumeId;
    private ResumeState resumeState;

    public static ResumeAggregate fromEvents(List<DomainEvent> events) {
        ResumeAggregate aggregate = new ResumeAggregate();
        events.forEach(aggregate::apply);
        return aggregate;
    }

    public void apply(DomainEvent event) {
        switch(event) {
            case ResumeCreationInitiated e -> {
                this.resumeId = e.getResumeId();
                this.resumeState = ResumeState.INITIATED;
            }
            default -> {

            }
        }
    }
}
