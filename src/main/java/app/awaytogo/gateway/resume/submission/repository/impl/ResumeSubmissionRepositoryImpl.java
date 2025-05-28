package app.awaytogo.gateway.resume.submission.repository.impl;

import app.awaytogo.gateway.resume.submission.repository.ExtendedResumeSubmissionRepository;
import app.awaytogo.gateway.resume.submission.repository.data.ResumeSubmissionEntity;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

public class ResumeSubmissionRepositoryImpl implements ExtendedResumeSubmissionRepository {
    private final ReactiveMongoTemplate reactiveMongoRepository;

    public ResumeSubmissionRepositoryImpl(ReactiveMongoTemplate reactiveMongoRepository) {
        this.reactiveMongoRepository = reactiveMongoRepository;
    }

    @Override
    public Mono<ResumeSubmissionEntity> findResumeSubmission(String submissionId) {
        Query query = Query.query(
                Criteria.where("profileId")
                        .is(submissionId));
        return reactiveMongoRepository.findOne(query, ResumeSubmissionEntity.class);
    }
}
