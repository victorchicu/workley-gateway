package ai.workley.core.chat.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface R2dbcChatParticipantRepository extends ReactiveCrudRepository<ChatParticipantEntity, Long> {

    Flux<ChatParticipantEntity> findAllByChatId(String chatId);
}
