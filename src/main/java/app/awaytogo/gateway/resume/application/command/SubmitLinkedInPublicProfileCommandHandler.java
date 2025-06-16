package app.awaytogo.gateway.resume.application.command;

import app.awaytogo.gateway.resume.application.CommandHandler;
import app.awaytogo.gateway.resume.domain.aggregate.ResumeAggregate;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitLinkedInPublicProfileCommand;
import app.awaytogo.gateway.resume.domain.command.impl.SubmitLinkedInPublicProfileResponse;
import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import app.awaytogo.gateway.resume.domain.service.ResumeAggregateService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@Component
public class SubmitLinkedInPublicProfileCommandHandler
        implements CommandHandler<SubmitLinkedInPublicProfileCommand, SubmitLinkedInPublicProfileResponse> {
    private final ResumeAggregateService resumeAggregateService;

    public SubmitLinkedInPublicProfileCommandHandler(ResumeAggregateService resumeAggregateService) {
        this.resumeAggregateService = resumeAggregateService;
    }

    @Override
    public Mono<SubmitLinkedInPublicProfileResponse> handle(Principal principal, SubmitLinkedInPublicProfileCommand command) {
        return resumeAggregateService.load(command.getResumeId())
                .defaultIfEmpty(
                        new ResumeAggregate()
                                .setResumeId(command.getResumeId())
                )
                .flatMap(resumeAggregate -> {
                    List<DomainEvent> events = resumeAggregate.handle(command);
                    return resumeAggregateService.save(resumeAggregate, events)
                            .thenReturn(new SubmitLinkedInPublicProfileResponse());
                });
    }
}
