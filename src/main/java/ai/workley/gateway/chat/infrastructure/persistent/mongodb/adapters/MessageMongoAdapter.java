package ai.workley.gateway.chat.infrastructure.persistent.mongodb.adapters;

import ai.workley.gateway.chat.application.ports.MessagePort;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.MessageRepository;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.MessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MessageMongoAdapter implements MessagePort {
    private final MessageRepository messageRepository;

    public MessageMongoAdapter(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Mono<Message<String>> save(Message<String> message) {
        MessageDocument<String> entity = toMessageDocument(message);
        return messageRepository.save(entity)
                .map(this::toMessage);
    }

    @Override
    public Flux<Message<String>> findAll(String chatId) {
        Pageable pageable = Pageable.ofSize(100);
        return messageRepository.findAllByChatId(chatId, pageable)
                .map(this::toMessage);
    }

    @Override
    public Flux<Message<String>> findRecentConversation(String chatId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return messageRepository.findLastN(chatId, pageable)
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
