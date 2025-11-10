package ai.workley.gateway.chat.infrastructure.messenger.mongodb;

import ai.workley.gateway.chat.domain.Chat;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import ai.workley.gateway.chat.infrastructure.messenger.MessengerRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoMessengerRepositoryAdapter implements MessengerRepository {
    private final MongoChatRepository mongoChatRepository;
    private final MongoMessageRepository mongoMessageRepository;

    public MongoMessengerRepositoryAdapter(MongoChatRepository mongoChatRepository, MongoMessageRepository mongoMessageRepository) {
        this.mongoChatRepository = mongoChatRepository;
        this.mongoMessageRepository = mongoMessageRepository;
    }

    @Override
    public Mono<Chat> save(Chat chat) {
        ChatDocument entity = toChatDocument(chat);
        return mongoChatRepository.save(entity)
                .map(this::toChat);
    }

    @Override
    public Mono<Chat> find(String chatId, Collection<String> participants) {
        return mongoChatRepository.findChat(chatId, participants)
                .map(this::toChat);
    }

    @Override
    public Mono<Message<? extends Content>> save(Message<? extends Content> message) {
        MessageDocument<? extends Content> entity = toMessageDocument(message);
        return mongoMessageRepository.save(entity)
                .map(this::toMessage);
    }

    @Override
    public Flux<Message<? extends Content>> loadAll(String chatId) {
        Pageable pageable = Pageable.ofSize(100);
        return mongoMessageRepository.findAllByChatId(chatId, pageable)
                .map(this::toMessage);
    }

    @Override
    public Flux<Message<? extends Content>> loadRecent(String chatId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return mongoMessageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId, pageable)
                .map(this::toMessage);
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

    private Message<? extends Content> toMessage(MessageDocument<? extends Content> source) {
        return Message.create(
                source.getMessageId(),
                source.getChatId(),
                source.getOwnedBy(),
                source.getRole(),
                source.getCreatedAt(),
                source.getContent()
        );
    }

    private MessageDocument<? extends Content> toMessageDocument(Message<? extends Content> source) {
        return MessageDocument.create(
                source.role(),
                source.chatId(),
                source.ownedBy(),
                source.id(),
                source.createdAt(),
                source.content()
        );
    }
}
