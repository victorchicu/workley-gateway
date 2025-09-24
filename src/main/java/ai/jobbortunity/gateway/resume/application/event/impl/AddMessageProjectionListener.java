package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.application.command.Message;
import ai.jobbortunity.gateway.resume.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.resume.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddMessageProjectionListener {
    private static final Logger log = LoggerFactory.getLogger(AddMessageProjectionListener.class);

    private final MessageHistoryRepository messageHistoryRepository;

    public AddMessageProjectionListener(MessageHistoryRepository messageHistoryRepository) {
        this.messageHistoryRepository = messageHistoryRepository;
    }

    private static Message<String> toMessage(MessageObject<String> source) {
        return Message.create(source.getId(), source.getChatId(), source.getAuthorId(), source.getRole(), source.getCreatedAt(), source.getContent());
    }

    private static MessageObject<String> createObject(AddMessageEvent source) {
        return MessageObject.create(
                source.message().id(), source.message().role(), source.message().chatId(), source.actor().getName(), source.message().createdAt(), source.message().content()
        );
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(AddMessageEvent source) {
        return messageHistoryRepository.save(createObject(source))
                .map(AddMessageProjectionListener::toMessage)
                .doOnSuccess(v -> log.info("Message added: {}", source))
                .doOnError(error -> log.error("Message not added: {}", source, error))
                .then();
    }
}