package io.zumely.gateway.resume.infrastructure;

import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageHistoryRepository extends ReactiveMongoRepository<MessageObject<?>, String> {

}
