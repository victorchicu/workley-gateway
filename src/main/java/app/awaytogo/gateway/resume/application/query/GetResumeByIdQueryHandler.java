package app.awaytogo.gateway.resume.application.query;

import app.awaytogo.gateway.resume.application.QueryHandler;
import app.awaytogo.gateway.resume.domain.model.impl.ResumeReadModel;
import app.awaytogo.gateway.resume.domain.query.impl.GetResumeByIdQuery;
import app.awaytogo.gateway.resume.infrastructure.repository.ResumeViewRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class GetResumeByIdQueryHandler implements QueryHandler<GetResumeByIdQuery, ResumeReadModel> {

    private final ResumeViewRepository resumeViewRepository;

    public GetResumeByIdQueryHandler(ResumeViewRepository resumeViewRepository) {
        this.resumeViewRepository = resumeViewRepository;
    }

    @Override
    public Mono<ResumeReadModel> handle(Principal principal, GetResumeByIdQuery query) {

        return resumeViewRepository.findResume(principal.getName(), query.resumeId());
    }
}