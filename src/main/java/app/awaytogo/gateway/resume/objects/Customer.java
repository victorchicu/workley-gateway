package app.awaytogo.gateway.resume.objects;

import app.awaytogo.gateway.resume.submissions.ResumeSubmissionRequest;
import app.awaytogo.gateway.resume.submissions.ResumeSubmissionResult;
import app.awaytogo.gateway.resume.submissions.ResumeSubmissionService;
import reactor.core.publisher.Mono;

public class Customer {
    private final ResumeSubmissionService resumeSubmissionService;

    public Customer(ResumeSubmissionService resumeSubmissionService) {
        this.resumeSubmissionService = resumeSubmissionService;
    }

    public Mono<ResumeSubmissionResult> submitResume(ResumeSubmissionRequest resumeSubmissionRequest) {
        //TODO: Save resume submission request object
        //TODO: Return submission id,
        throw new UnsupportedOperationException();
    }

    public Mono<ResumeSubmissionResult> findResumeSubmission(String submissionId) {
        throw new UnsupportedOperationException();
    }
}
