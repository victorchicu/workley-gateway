package app.awaytogo.gateway.resume.submission.repository;

import app.awaytogo.gateway.resume.submission.repository.data.ResumeSubmissionEntity;
import reactor.core.publisher.Mono;

public interface ExtendedResumeSubmissionRepository {
    Mono<ResumeSubmissionEntity> findResumeSubmission(String submissionId);
}
