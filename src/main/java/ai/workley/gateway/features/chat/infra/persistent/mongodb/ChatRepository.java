package ai.workley.gateway.features.chat.infra.persistent.mongodb;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.ChatDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface ChatRepository extends ReactiveMongoRepository<ChatDocument, String> {

    @Query("{ 'chatId': ?0, 'participants.id': { $all: ?1 } }")
    Mono<ChatDocument> findChat(String id, Collection<String> participants);
}