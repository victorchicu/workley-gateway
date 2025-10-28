package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.event.ChatCreated;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.ChatRepository;
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

    private final ChatRepository chatRepository;

    public CreateChatProjection(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(ChatCreated e) {
        return chatRepository.save(ChatDocument.create(e.chatId(), Summary.create(e.prompt()), Set.of(Participant.create(e.actor()))))
                .doOnSuccess((ChatDocument chatDocument) -> log.info("Chat created (actor={}, chatId={})", e.actor(), e.chatId()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.error("Failed to create chat (actor={}, chatId={})", e.actor(), e.chatId(), error);
                    return Mono.empty();
                })
                .then();
    }
}
