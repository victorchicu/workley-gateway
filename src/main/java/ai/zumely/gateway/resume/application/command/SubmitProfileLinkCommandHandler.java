package ai.zumely.gateway.resume.application.command;

import ai.zumely.gateway.resume.application.CommandHandler;
import ai.zumely.gateway.resume.domain.aggregate.ResumeAggregate;
import ai.zumely.gateway.resume.domain.command.impl.SubmitProfileLinkCommand;
import ai.zumely.gateway.resume.domain.command.impl.SubmitProfileLinkResponse;
import ai.zumely.gateway.resume.domain.event.DomainEvent;
import ai.zumely.gateway.resume.infrastructure.eventstore.ResumeEventStore;
import ai.zumely.gateway.resume.domain.service.ResumeAggregateService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@Component
public class SubmitProfileLinkCommandHandler implements CommandHandler<SubmitProfileLinkCommand, SubmitProfileLinkResponse> {
    private final ResumeEventStore resumeEventStore;
    private final ResumeAggregateService resumeAggregateService;

    public SubmitProfileLinkCommandHandler(ResumeEventStore resumeEventStore, ResumeAggregateService resumeAggregateService) {
        this.resumeEventStore = resumeEventStore;
        this.resumeAggregateService = resumeAggregateService;
    }

    @Override
    public Mono<SubmitProfileLinkResponse> handle(Principal principal, SubmitProfileLinkCommand command) {
        return resumeAggregateService.load(command.getResumeId())
                .defaultIfEmpty(
                        new ResumeAggregate()
                                .setResumeId(command.getResumeId())
                )
                .flatMap(resumeAggregate -> {
                    List<DomainEvent> events = resumeAggregate.handle(command);
                    return resumeEventStore.saveEvents(resumeAggregate, events)
                            .thenReturn(new SubmitProfileLinkResponse());
                });
    }
}