package io.zumely.gateway.resume.infrastructure;

import io.zumely.gateway.resume.infrastructure.data.ChatObject;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface ChatSessionRepository extends ReactiveMongoRepository<ChatObject, String> {

    @Query("{ '_id': ?0, 'participants._id': { $all: ?1 } }")
    Mono<ChatObject> findChat(String id, Collection<String> participants);
}