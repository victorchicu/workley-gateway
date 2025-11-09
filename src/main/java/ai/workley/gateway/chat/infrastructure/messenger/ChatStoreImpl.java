package ai.workley.gateway.chat.infrastructure.messenger;

import ai.workley.gateway.chat.application.ports.outbound.ChatStore;
import ai.workley.gateway.chat.domain.Chat;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class ChatStoreImpl implements ChatStore {
    private final MessengerRepository messengerRepository;

    public ChatStoreImpl(MessengerRepository messengerRepository) {
        this.messengerRepository = messengerRepository;
    }

    @Override
    public Mono<Chat> save(Chat chat) {
        return messengerRepository.save(chat);
    }

    @Override
    public Mono<Chat> findChat(String id, Collection<String> participants) {
        return messengerRepository.findChat(id, participants);
    }
}
