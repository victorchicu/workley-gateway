package ai.workley.gateway.features.chat.infra.persistent.mongodb.adapter;

import ai.workley.gateway.features.chat.app.port.ChatPort;
import ai.workley.gateway.features.chat.domain.Chat;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.ChatRepository;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.ChatDocument;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChatAdapter implements ChatPort {
    private final ChatRepository chatRepository;

    public ChatAdapter(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Mono<Chat> save(Chat chat) {
        ChatDocument entity = toChatDocument(chat);
        return chatRepository.save(entity)
                .map(this::toChat);
    }

    @Override
    public Mono<Chat> findChat(String id, Collection<String> participants) {
        return chatRepository.findChat(id, participants)
                .map(this::toChat);
    }

    private Chat toChat(ChatDocument source) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ChatDocument toChatDocument(Chat source) {
        ChatDocument.Summary summary =
                ChatDocument.Summary.create(source.summary().getTitle());

        Set<ChatDocument.Participant> participants =
                source.participants().stream()
                        .map((Chat.Participant participant) -> ChatDocument.Participant.create(participant.getId()))
                        .collect(Collectors.toSet());

        return new ChatDocument()
                .setChatId(source.id())
                .setSummary(summary)
                .setParticipants(participants);
    }
}
