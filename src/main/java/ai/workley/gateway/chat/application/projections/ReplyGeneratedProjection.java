package ai.workley.gateway.chat.application.projections;

import ai.workley.gateway.chat.application.ports.MessagePort;
import ai.workley.gateway.chat.domain.exceptions.InfrastructureErrors;
import ai.workley.gateway.chat.domain.events.ReplyGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReplyGeneratedProjection {
    private static final Logger log = LoggerFactory.getLogger(ReplyGeneratedProjection.class);

    private final MessagePort messagePort;

    public ReplyGeneratedProjection(MessagePort messagePort) {
        this.messagePort = messagePort;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(ReplyGenerated e) {
        return messagePort.save(e.reply())
                .doOnSuccess(saved ->
                        log.info("Reply saved: (actor={}, chatId={}, messageId={})",
                                e.actor(), saved.chatId(), saved.id()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Reply already exists (actor={}, chatId={}, messageId={})",
                            e.actor(), e.chatId(), e.reply().id(), error);
                    return Mono.empty();
                })
                .then();
    }
}
