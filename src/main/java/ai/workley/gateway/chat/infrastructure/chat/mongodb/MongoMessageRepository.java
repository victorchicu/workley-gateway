package ai.workley.gateway.chat.infrastructure.chat.mongodb;

import ai.workley.gateway.chat.domain.content.Content;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MongoMessageRepository extends ReactiveMongoRepository<MessageDocument<? extends Content>, String> {

    Flux<MessageDocument<? extends Content>> findAllByChatId(String chatId, Pageable pageable);

    Flux<MessageDocument<? extends Content>> findAllByChatIdOrderByCreatedAtAsc(String chatId, Pageable pageable);
}
