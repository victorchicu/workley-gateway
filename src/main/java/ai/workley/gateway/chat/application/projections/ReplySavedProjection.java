package ai.workley.gateway.chat.application.projections;

import ai.workley.gateway.chat.application.ports.MessagePort;
import ai.workley.gateway.chat.domain.events.ReplySaved;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReplySavedProjection {
    private static final Logger log = LoggerFactory.getLogger(ReplySavedProjection.class);

    private final MessagePort messagePort;

    public ReplySavedProjection(MessagePort messagePort) {
        this.messagePort = messagePort;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(ReplySaved e) {
        return messagePort.save(e.message())
                .doOnSuccess(saved ->
                        log.info("Reply saved: (actor={}, chatId={}, messageId={})",
                                e.actor(), saved.chatId(), saved.id()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Reply already exists (actor={}, chatId={}, messageId={})",
                            e.actor(), e.chatId(), e.message().id(), error);
                    return Mono.empty();
                })
                .then();
    }
}
