package ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.repository;

import ai.jobbortunity.gateway.chat.infrastructure.persistent.readmodel.entity.ChatModel;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface ChatReadRepository extends ReactiveMongoRepository<ChatModel, String> {

    @Query("{ 'chatId': ?0, 'participants.participantId': { $all: ?1 } }")
    Mono<ChatModel> findChat(String id, Collection<String> participants);
}
