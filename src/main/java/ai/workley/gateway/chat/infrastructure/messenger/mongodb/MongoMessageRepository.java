package ai.workley.gateway.chat.infrastructure.messenger.mongodb;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MongoMessageRepository extends ReactiveMongoRepository<MessageDocument<String>, String> {

    Flux<MessageDocument<String>> findAllByChatId(String chatId, Pageable pageable);

    Flux<MessageDocument<String>> findAllByChatIdOrderByCreatedAtAsc(String chatId, Pageable pageable);


}
