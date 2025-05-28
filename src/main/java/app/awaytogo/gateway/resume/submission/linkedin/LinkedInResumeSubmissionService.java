package app.awaytogo.gateway.resume.submission.linkedin;

import app.awaytogo.gateway.resume.submission.ResumeSubmission;
import app.awaytogo.gateway.resume.submission.ResumeSubmissionResult;
import app.awaytogo.gateway.resume.submission.ResumeSubmissionService;
import app.awaytogo.gateway.resume.submission.repository.ResumeSubmissionSubmissionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LinkedInResumeSubmissionService implements ResumeSubmissionService {
    private final ResumeSubmissionSubmissionRepository resumeSubmissionRepository;

    public LinkedInResumeSubmissionService(ResumeSubmissionSubmissionRepository resumeSubmissionRepository) {
        this.resumeSubmissionRepository = resumeSubmissionRepository;
    }

    @Override
    public Mono<ResumeSubmissionResult> submit(ResumeSubmission resumeSubmission) {
        throw new UnsupportedOperationException();
    }
}
