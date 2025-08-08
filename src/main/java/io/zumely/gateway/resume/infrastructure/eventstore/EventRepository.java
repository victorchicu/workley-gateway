package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.infrastructure.eventstore.entity.StoredEvent;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EventRepository extends ReactiveMongoRepository<StoredEvent<?>, String> {

    @Query("{ 'actor': ?0, 'event.chatId': ?1 }")
    Flux<StoredEvent<?>> findChat(String actorId, String chatId);
}