package ai.workley.gateway.chat.infrastructure.chat;

import ai.workley.gateway.chat.application.ports.outbound.messenger.MessageStore;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
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
    public Mono<Message<? extends Content>> save(Message<? extends Content> message) {
        return messengerRepository.save(message);
    }

    @Override
    public Flux<Message<? extends Content>> loadAll(String chatId) {
        return messengerRepository.loadAll(chatId);
    }


    @Override
    public Flux<Message<? extends Content>> loadRecent(String chatId, int limit) {
        return messengerRepository.loadRecent(chatId, limit);
    }
}
