package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.infrastructure.exception.InfrastructureExceptions;
import ai.jobbortunity.gateway.chat.infrastructure.ChatSessionRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.ChatObject;
import ai.jobbortunity.gateway.chat.infrastructure.data.ParticipantObject;
import ai.jobbortunity.gateway.chat.infrastructure.data.SummaryObject;
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

    private final ChatSessionRepository chatSessionRepository;

    public CreateChatProjection(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(CreateChatEvent e) {
        return chatSessionRepository.save(ChatObject.create(e.chatId(), SummaryObject.create(e.prompt()), Set.of(ParticipantObject.create(e.actor()))))
                .doOnSuccess((ChatObject chatObject) -> log.info("Chat created (actor={}, chatId={})", e.actor(), e.chatId()))
                .onErrorResume(InfrastructureExceptions::isDuplicateKey, error -> {
                    log.error("Failed to create chat (actor={}, chatId={})", e.actor(), e.chatId(), error);
                    return Mono.empty();
                })
                .then();
    }
}