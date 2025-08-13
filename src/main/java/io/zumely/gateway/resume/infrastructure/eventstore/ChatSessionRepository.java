package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.infrastructure.eventstore.data.ChatObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ChatSessionRepository extends ReactiveMongoRepository<ChatObject, String> {

    Mono<ChatObject> findChatObjectById(String id);
}