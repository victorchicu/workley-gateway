package ai.workley.gateway.chat.infrastructure.chat.mongodb;

import ai.workley.gateway.chat.application.ports.outbound.messaging.ChatStore;
import ai.workley.gateway.chat.domain.Chat;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoChatRepositoryAdapter implements ChatStore {
    private final MongoChatRepository mongoChatRepository;

    public MongoChatRepositoryAdapter(MongoChatRepository mongoChatRepository) {
        this.mongoChatRepository = mongoChatRepository;
    }

    @Override
    public Mono<Chat> saveChat(Chat chat) {
        ChatDocument entity = toChatDocument(chat);
        return mongoChatRepository.save(entity)
                .map(this::toChat);
    }

    @Override
    public Mono<Chat> findChat(String chatId, Collection<String> participants) {
        return mongoChatRepository.findChat(chatId, participants)
                .map(this::toChat);
    }

    private Chat toChat(ChatDocument source) {
        Set<Chat.Participant> participants =
                source.getParticipants().stream()
                        .map(participant -> Chat.Participant.create(participant.getId()))
                        .collect(Collectors.toSet());

        return Chat.create(source.getChatId(), Chat.Summary.create(source.getSummary().getTitle()), participants);
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