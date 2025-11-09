package ai.workley.gateway.chat.infrastructure.data.mongodb.service;

import ai.workley.gateway.chat.application.ports.outbound.ChatService;
import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.infrastructure.data.mongodb.document.ChatDocument;
import ai.workley.gateway.chat.infrastructure.data.mongodb.repository.ChatRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoDbChatService implements ChatService {
    private final ChatRepository chatRepository;

    public MongoDbChatService(ChatRepository chatRepository) {
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
