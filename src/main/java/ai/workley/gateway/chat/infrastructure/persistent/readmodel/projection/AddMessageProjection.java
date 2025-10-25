package ai.workley.gateway.chat.infrastructure.persistent.readmodel.projection;

import ai.workley.gateway.chat.domain.event.MessageAdded;
import ai.workley.gateway.chat.infrastructure.error.InfrastructureErrors;
import ai.workley.gateway.chat.domain.model.Message;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.repository.MessageReadRepository;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddMessageProjection {
    private static final Logger log = LoggerFactory.getLogger(AddMessageProjection.class);

    private final MessageReadRepository messageReadRepository;

    public AddMessageProjection(MessageReadRepository messageReadRepository) {
        this.messageReadRepository = messageReadRepository;
    }

    private static Message<String> toMessage(MessageModel<String> source) {
        return Message.response(source.getId(), source.getChatId(), source.getOwnedBy(), source.getRole(), source.getCreatedAt(), source.getContent());
    }

    private static MessageModel<String> toMessageModel(MessageAdded source) {
        return MessageModel.create(
                source.message().role(), source.message().chatId(), source.actor(), source.message().id(), source.message().createdAt(), source.message().content()
        );
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(MessageAdded e) {
        return messageReadRepository.save(toMessageModel(e))
                .map(message -> {
                    log.info("Message was saved successfully (actor={}, chatId={}, messageId={})",
                            message.getOwnedBy(), message.getChatId(), message.getMessageId());
                    return toMessage(message);
                })
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.error("Failed to add prompt (actor={}, chatId={}, messageId={})",
                            e.actor(), e.chatId(), e.message().id(), error);
                    return Mono.empty();
                })
                .then();
    }
}
