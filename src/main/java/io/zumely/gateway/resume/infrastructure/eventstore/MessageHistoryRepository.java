package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.infrastructure.eventstore.data.MessageObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageHistoryRepository extends ReactiveMongoRepository<MessageObject<?>, String> {

}
