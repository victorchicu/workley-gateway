package app.awaytogo.gateway.resume.submissions;

import reactor.core.publisher.Mono;

public interface ResumeSubmissionService {

    Mono<ResumeSubmissionResult> submit(ResumeSubmission resumeSubmission);
}
