package ai.workley.gateway.chat.infrastructure.persistent.readmodel.projection;

import ai.workley.gateway.chat.domain.event.ChatCreated;
import ai.workley.gateway.chat.infrastructure.error.InfrastructureErrors;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.repository.ChatReadRepository;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity.ChatModel;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity.ParticipantModel;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity.SummaryModel;
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
