package ai.workley.gateway.features.chat.infra.persistent.mongodb;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.MessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<MessageDocument<String>, String> {

    @Query(value = "{ 'chatId': ?0 }", sort = "{ 'createdAt': 1 }")
    Flux<MessageDocument<String>> findLastN(String chatId, Pageable pageable);

    Flux<MessageDocument<String>> findAllByChatId(String chatId, Pageable pageable);
}