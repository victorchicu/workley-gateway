package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.chat.app.port.MessagePort;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.event.MessageAdded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AddMessageProjection {
    private static final Logger log = LoggerFactory.getLogger(AddMessageProjection.class);

    private final MessagePort messagePort;

    public AddMessageProjection(MessagePort messagePort) {
        this.messagePort = messagePort;
    }

    @Async
    @EventListener
    @Order(0)
    public void handle(MessageAdded e) {
        messagePort.save(e.message())
                .doOnSuccess(message -> {
                    log.info("Message saved (actor={}, chatId={}, messageId={})",
                            message.ownedBy(), message.chatId(), message.id());
                })
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Message already exists (actor={}, chatId={}, messageId={})",
                            e.actor(), e.chatId(), e.message().id(), error);
                    return Mono.empty();
                })
                .subscribe();
    }
}
