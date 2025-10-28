package ai.workley.gateway.features.chat.infra.persistent.mongodb;

import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.MessageDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveMongoRepository<MessageDocument<String>, String> {

    Flux<MessageDocument<String>> findAllByChatId(String chatId);
}
