package ai.workley.gateway.features.chat.infra.projection;

import ai.workley.gateway.features.chat.application.*;
import ai.workley.gateway.features.chat.domain.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.event.ChatCreated;
import ai.workley.gateway.features.chat.infra.persistent.ChatReadRepository;
import ai.workley.gateway.features.chat.infra.readmodel.ChatModel;
import ai.workley.gateway.features.chat.infra.readmodel.ParticipantModel;
import ai.workley.gateway.features.chat.infra.readmodel.SummaryModel;
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

    private final ChatReadRepository chatReadRepository;

    public CreateChatProjection(ChatReadRepository chatReadRepository) {
        this.chatReadRepository = chatReadRepository;
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(ChatCreated e) {
        return chatReadRepository.save(ChatModel.create(e.chatId(), SummaryModel.create(e.prompt()), Set.of(ParticipantModel.create(e.actor()))))
                .doOnSuccess((ChatModel chatModel) -> log.info("Chat created (actor={}, chatId={})", e.actor(), e.chatId()))
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.error("Failed to create chat (actor={}, chatId={})", e.actor(), e.chatId(), error);
                    return Mono.empty();
                })
                .then();
    }
}
