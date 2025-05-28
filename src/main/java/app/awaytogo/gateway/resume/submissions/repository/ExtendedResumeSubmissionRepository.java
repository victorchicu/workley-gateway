package app.awaytogo.gateway.resume.submissions.repository;

import app.awaytogo.gateway.resume.submissions.repository.data.ResumeSubmissionEntity;
import reactor.core.publisher.Mono;

public interface ExtendedResumeSubmissionRepository {
    Mono<ResumeSubmissionEntity> findResumeSubmission(String submissionId);
}
