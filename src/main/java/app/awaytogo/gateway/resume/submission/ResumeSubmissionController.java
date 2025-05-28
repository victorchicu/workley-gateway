package app.awaytogo.gateway.resume.submission;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/resume/submissions")
public class ResumeSubmissionController {
    private final ResumeSubmissionService resumeSubmissionService;

    public ResumeSubmissionController(ResumeSubmissionService resumeSubmissionService) {
        this.resumeSubmissionService = resumeSubmissionService;
    }

    @PostMapping
    public Mono<ResumeSubmissionResult> submitResume(ResumeSubmissionRequest resumeSubmissionRequest) {
        return resumeSubmissionService.submit(
                new ResumeSubmission());
    }

    public Mono<ResumeSubmissionResult> findResumeSubmission() {
        throw new UnsupportedOperationException();
    }
}
