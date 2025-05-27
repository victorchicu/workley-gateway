package app.awaytogo.gateway.resume.repository;

import app.awaytogo.gateway.resume.repository.data.ResumeEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends ReactiveMongoRepository<String, ResumeEntity>, ExtendedResumeRepository {

}
