package ai.workley.gateway.chat.infrastructure.chat.mongodb;

import ai.workley.gateway.chat.application.ports.outbound.messaging.MessageStore;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.content.Content;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoMessageRepositoryAdapter implements MessageStore {
    private final MongoMessageRepository mongoMessageRepository;

    public MongoMessageRepositoryAdapter(MongoMessageRepository mongoMessageRepository) {
        this.mongoMessageRepository = mongoMessageRepository;
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
