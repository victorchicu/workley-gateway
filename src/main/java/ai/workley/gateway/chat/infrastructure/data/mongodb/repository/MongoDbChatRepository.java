package ai.workley.gateway.chat.infrastructure.data.mongodb.repository;

import ai.workley.gateway.chat.infrastructure.data.mongodb.document.ChatDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface MongoDbChatRepository extends ReactiveMongoRepository<ChatDocument, String> {

    @Query("{ 'chatId': ?0, 'participants.id': { $all: ?1 } }")
    Mono<ChatDocument> findChat(String id, Collection<String> participants);
}
