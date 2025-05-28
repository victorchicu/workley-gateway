package app.awaytogo.gateway.resume.submissions.linkedin;

import app.awaytogo.gateway.resume.submissions.ResumeSubmission;
import app.awaytogo.gateway.resume.submissions.ResumeSubmissionResult;
import app.awaytogo.gateway.resume.submissions.ResumeSubmissionService;
import app.awaytogo.gateway.resume.submissions.repository.ResumeSubmissionSubmissionRepository;
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
