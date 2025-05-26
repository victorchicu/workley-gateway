package app.awaytogo.gateway.resume;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResumeProfileDraftRepository extends MongoRepository<String, ResumeEntity> {
}
