package ai.workley.gateway.features.chat.infra.persistent;

import ai.workley.gateway.features.chat.infra.readmodel.MessageModel;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageReadRepository extends ReactiveMongoRepository<MessageModel<String>, String> {

    Flux<MessageModel<String>> findAllByChatId(String chatId);
}
