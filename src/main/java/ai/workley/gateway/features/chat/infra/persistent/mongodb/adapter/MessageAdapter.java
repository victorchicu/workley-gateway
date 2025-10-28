package ai.workley.gateway.features.chat.infra.persistent.mongodb.adapter;

import ai.workley.gateway.features.chat.app.port.MessagePort;
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
    public Mono<MessageDocument<String>> save(MessageDocument<String> message) {
        return messageRepository.save(message);
    }

    @Override
    public Flux<MessageDocument<String>> findAll(String chatId) {
        return messageRepository.findAllByChatId(chatId);
    }
}
