package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.chat.app.port.MessagePort;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.MessageDocument;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.event.MessageAdded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddMessageProjection {
    private static final Logger log = LoggerFactory.getLogger(AddMessageProjection.class);

    private final MessagePort messagePort;

    public AddMessageProjection(MessagePort messagePort) {
        this.messagePort = messagePort;
    }

    private static Message<String> toMessage(MessageDocument<String> source) {
        return Message.response(source.getId(), source.getChatId(), source.getOwnedBy(), source.getRole(), source.getCreatedAt(), source.getContent());
    }

    private static MessageDocument<String> toMessageDocument(MessageAdded source) {
        return MessageDocument.create(
                source.message().role(), source.message().chatId(), source.actor(), source.message().id(), source.message().createdAt(), source.message().content()
        );
    }

    @EventListener
    @Order(0)
    public Mono<Void> handle(MessageAdded e) {
        return messagePort.save(toMessageDocument(e))
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
