package ai.workley.gateway.chat.application.projections;

import ai.workley.gateway.chat.application.ports.outbound.MessageService;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageAddedProjection {
    private static final Logger log = LoggerFactory.getLogger(MessageAddedProjection.class);

    private final MessageService messageService;

    public MessageAddedProjection(MessageService messageService) {
        this.messageService = messageService;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(MessageAdded e) {
        return messageService.save(e.message())
                .doOnSuccess(message -> {
                    log.info("Message saved (actor={}, chatId={}, messageId={})",
                            message.ownedBy(), message.chatId(), message.id());
                })
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Message already exists (actor={}, chatId={}, messageId={})",
                            e.actor(), e.chatId(), e.message().id(), error);
                    return Mono.empty();
                })
                .then();
    }
}
