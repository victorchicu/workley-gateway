package app.awaytogo.gateway.resume.api;

import app.awaytogo.gateway.resume.api.dto.ViewResumeReadModelDto;
import app.awaytogo.gateway.resume.application.QueryDispatcher;
import app.awaytogo.gateway.resume.domain.model.impl.ResumeReadModel;
import app.awaytogo.gateway.resume.api.exception.ResumeNotFoundApiException;
import app.awaytogo.gateway.resume.domain.query.impl.GetResumeByIdQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/query/resumes")
public class ResumeQueryController {

    private static final Logger log = LoggerFactory.getLogger(ResumeQueryController.class);

    private final ConversionService conversionService;
    private final QueryDispatcher queryDispatcher;

    public ResumeQueryController(QueryDispatcher queryDispatcher, ConversionService conversionService) {
        this.queryDispatcher = queryDispatcher;
        this.conversionService = conversionService;
    }

    @GetMapping("/{resumeId}")
    public Mono<ViewResumeReadModelDto> viewResume(Principal principal, @PathVariable String resumeId) {
        return queryDispatcher.<ResumeReadModel>dispatch(principal, new GetResumeByIdQuery(resumeId))
                .map(this::toResumeReadModelDto)
                .doOnSuccess(resume -> log.debug("Found resume: {}", resumeId))
                .switchIfEmpty(Mono.error(new ResumeNotFoundApiException("Resume not found: " + resumeId)));
    }


    private ViewResumeReadModelDto toResumeReadModelDto(ResumeReadModel resumeReadModel) {
        return conversionService.convert(resumeReadModel, ViewResumeReadModelDto.class);
    }
}
