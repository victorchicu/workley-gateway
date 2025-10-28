package ai.workley.gateway.features.chat.infra.persistent.mongodb.adapter;

import ai.workley.gateway.features.chat.app.port.MessagePort;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.MessageRepository;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.MessageDocument;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MessageAdapter implements MessagePort {
    private final MessageRepository messageRepository;

    public MessageAdapter(MessageRepository messageRepository) {
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
        return messageRepository.findAllByChatId(chatId)
                .map(this::toMessage);
    }

    private Message<String> toMessage(MessageDocument<String> source) {
        return Message.create(
                source.getId(), source.getChatId(), source.getOwnedBy(), source.getRole(), source.getCreatedAt(), source.getContent()
        );
    }

    private MessageDocument<String> toMessageDocument(Message<String> source) {
        return MessageDocument.create(
                source.role(), source.chatId(), source.ownedBy(), source.id(), source.createdAt(), source.content()
        );
    }
}
