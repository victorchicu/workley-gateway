package ai.zumely.gateway.resume.application.query;

import ai.zumely.gateway.resume.application.QueryHandler;
import ai.zumely.gateway.resume.domain.model.impl.ResumeReadModel;
import ai.zumely.gateway.resume.domain.query.impl.GetResumeByIdQuery;
import ai.zumely.gateway.resume.infrastructure.repository.ResumeViewRepository;
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