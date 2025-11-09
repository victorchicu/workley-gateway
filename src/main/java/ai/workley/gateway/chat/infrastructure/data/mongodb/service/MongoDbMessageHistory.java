package ai.workley.gateway.chat.infrastructure.data.mongodb.service;

import ai.workley.gateway.chat.application.ports.outbound.MessageHistory;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.data.mongodb.document.MessageDocument;
import ai.workley.gateway.chat.infrastructure.data.mongodb.repository.MongoDbMessageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MongoDbMessageHistory implements MessageHistory {
    private final MongoDbMessageRepository mongoDbMessageRepository;

    public MongoDbMessageHistory(MongoDbMessageRepository mongoDbMessageRepository) {
        this.mongoDbMessageRepository = mongoDbMessageRepository;
    }

    @Override
    public Mono<Message<String>> save(Message<String> message) {
        MessageDocument<String> entity = toMessageDocument(message);
        return mongoDbMessageRepository.save(entity)
                .map(this::toMessage);
    }

    @Override
    public Flux<Message<String>> loadAll(String chatId) {
        Pageable pageable = Pageable.ofSize(100);
        return mongoDbMessageRepository.findAllByChatId(chatId, pageable)
                .map(this::toMessage);
    }

    @Override
    public Flux<Message<String>> loadRecent(String chatId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return mongoDbMessageRepository.findAllByChatIdOrderByCreatedAtAsc(chatId, pageable)
                .map(this::toMessage);
    }


    private Message<String> toMessage(MessageDocument<String> source) {
        return Message.create(
                source.getMessageId(),
                source.getChatId(),
                source.getOwnedBy(),
                source.getRole(),
                source.getCreatedAt(),
                source.getContent()
        );
    }

    private MessageDocument<String> toMessageDocument(Message<String> source) {
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
