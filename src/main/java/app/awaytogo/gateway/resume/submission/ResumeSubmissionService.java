package app.awaytogo.gateway.resume.submission;

import reactor.core.publisher.Mono;

public interface ResumeSubmissionService {

    Mono<ResumeSubmissionResult> submit(ResumeSubmission resumeSubmission);
}
