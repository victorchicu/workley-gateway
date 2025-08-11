package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.infrastructure.eventstore.data.StoredEvent;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EventRepository extends ReactiveMongoRepository<StoredEvent<?>, String> {

    @Query("{ 'actor': ?0, 'data.chatId': ?1 }")
    Flux<StoredEvent<?>> findEventsByChatId(String actorId, String chatId);
}
