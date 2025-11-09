package ai.workley.gateway.chat.infrastructure.messenger;

import ai.workley.gateway.chat.application.ports.outbound.MessageStore;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.messenger.mongodb.MessageDocument;
import ai.workley.gateway.chat.infrastructure.messenger.mongodb.MongoMessageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MessageStoreImpl implements MessageStore {
    private final MessengerRepository messengerRepository;

    public MessageStoreImpl(MessengerRepository messengerRepository) {
        this.messengerRepository = messengerRepository;
    }

    @Override
    public Mono<Message<String>> save(Message<String> message) {
        return messengerRepository.save(message);
    }

    @Override
    public Flux<Message<String>> loadAll(String chatId) {
        return messengerRepository.loadAll(chatId);
    }


    @Override
    public Flux<Message<String>> loadRecent(String chatId, int limit) {
        return messengerRepository.loadRecent(chatId, limit);
    }
}
