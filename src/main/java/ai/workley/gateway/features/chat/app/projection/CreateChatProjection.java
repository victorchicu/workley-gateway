package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.chat.app.port.ChatPort;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.event.ChatCreated;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.ChatDocument;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.Participant;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class CreateChatProjection {
    private static final Logger log = LoggerFactory.getLogger(CreateChatProjection.class);

    private final ChatPort chatPort;

    public CreateChatProjection(ChatPort chatPort) {
        this.chatPort = chatPort;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(ChatCreated e) {
        return chatPort.save(ChatDocument.create(e.chatId(), Summary.create(e.prompt()), Set.of(Participant.create(e.actor()))))
                .doOnSuccess((ChatDocument chatDocument) -> log.info("Chat created (actor={}, chatId={})", e.actor(), e.chatId()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Chat already exists (actor={}, chatId={})", e.actor(), e.chatId(), error);
                    return Mono.empty();
                })
                .then();
    }
}
