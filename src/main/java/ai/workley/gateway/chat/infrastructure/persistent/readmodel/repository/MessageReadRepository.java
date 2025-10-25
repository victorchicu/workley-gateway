package ai.workley.gateway.chat.infrastructure.persistent.readmodel.repository;

import ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity.MessageModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageReadRepository extends ReactiveMongoRepository<MessageModel<String>, String> {

    Flux<MessageModel<String>> findAllByChatId(String chatId);
}
