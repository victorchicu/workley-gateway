package app.awaytogo.gateway.resume.repository.impl;

import app.awaytogo.gateway.resume.repository.ExtendedResumeRepository;
import app.awaytogo.gateway.resume.repository.data.ResumeEntity;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

public class ResumeRepositoryImpl implements ExtendedResumeRepository {
    private final ReactiveMongoTemplate reactiveMongoRepository;

    public ResumeRepositoryImpl(ReactiveMongoTemplate reactiveMongoRepository) {
        this.reactiveMongoRepository = reactiveMongoRepository;
    }

    @Override
    public Mono<ResumeEntity> findResume(String profileId) {
        Query query = Query.query(
                Criteria.where("profileId")
                        .is(profileId));
        return reactiveMongoRepository.findOne(query, ResumeEntity.class);
    }
}
