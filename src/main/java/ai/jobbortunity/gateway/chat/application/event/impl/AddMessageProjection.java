package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.infrastructure.exception.InfrastructureExceptions;
import ai.jobbortunity.gateway.chat.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddMessageProjection {
    private static final Logger log = LoggerFactory.getLogger(AddMessageProjection.class);

    private final MessageHistoryRepository messageHistoryRepository;

    public AddMessageProjection(MessageHistoryRepository messageHistoryRepository) {
        this.messageHistoryRepository = messageHistoryRepository;
    }

    private static Message<String> toMessage(MessageObject<String> source) {
        return Message.response(source.getId(), source.getChatId(), source.getOwnedBy(), source.getRole(), source.getCreatedAt(), source.getContent());
    }

    private static MessageObject<String> createObject(AddMessageEvent source) {
        return MessageObject.create(
                source.message().role(), source.message().chatId(), source.actor(), source.message().id(), source.message().createdAt(), source.message().content()
        );
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(AddMessageEvent e) {
        return messageHistoryRepository.save(createObject(e))
                .map(message -> {
                    log.info("Message was saved successfully (actor={}, chatId={}, messageId={})",
                            message.getOwnedBy(), message.getChatId(), message.getMessageId());
                    return toMessage(message);
                })
                .onErrorResume(InfrastructureExceptions::isDuplicateKey, error -> {
                    log.error("Failed to add prompt (actor={}, chatId={}, messageId={})",
                            e.actor(), e.chatId(), e.message().id(), error);
                    return Mono.empty();
                })
                .then();
    }
}