package ai.jobbortunity.gateway.chat.infrastructure;

import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageHistoryRepository extends ReactiveMongoRepository<MessageObject<String>, String> {

    Flux<MessageObject<String>> findAllByChatId(String chatId);
}
