package ai.workley.gateway.features.chat.infra.persistent.mongodb.adapter;

import ai.workley.gateway.features.chat.app.port.ChatPort;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.ChatRepository;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.ChatDocument;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class ChatAdapter implements ChatPort {
    private final ChatRepository chatRepository;

    public ChatAdapter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Mono<ChatDocument> save(ChatDocument chat) {
        return chatRepository.save(chat);
    }

    @Override
    public Mono<ChatDocument> findChat(String id, Collection<String> participants) {
        return chatRepository.findChat(id, participants);
    }
}
