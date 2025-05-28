package app.awaytogo.gateway.resume.submission.repository;

import app.awaytogo.gateway.resume.submission.repository.data.ResumeSubmissionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSubmissionSubmissionRepository extends ReactiveMongoRepository<String, ResumeSubmissionEntity>, ExtendedResumeSubmissionRepository {

}
