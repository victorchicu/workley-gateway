package ai.zumely.gateway.resume.infrastructure.repository;

import ai.zumely.gateway.resume.domain.model.impl.ResumeReadModel;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ResumeViewRepository extends ReactiveMongoRepository<ResumeReadModel, String> {

    @Query("{ 'userId': ?0, 'resumeId': ?1 }")
    Mono<ResumeReadModel> findResume(String userId, String resumeId);
}
