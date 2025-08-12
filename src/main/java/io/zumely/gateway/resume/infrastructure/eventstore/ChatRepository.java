package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreObject;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChatRepository extends ReactiveMongoRepository<StoreObject<?>, String> {

    @Query("{ 'actor': ?0, 'data.chatId': ?1 }")
    Mono<Boolean> existsBy(String actorId, String chatId);

    @Query("{ 'actor': ?0, 'data.chatId': ?1 }")
    Flux<StoreObject<?>> findAllBy(String actorId, String chatId);
}