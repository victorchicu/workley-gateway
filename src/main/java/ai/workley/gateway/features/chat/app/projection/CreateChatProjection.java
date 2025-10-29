package ai.workley.gateway.features.chat.app.projection;

import ai.workley.gateway.features.chat.app.port.ChatPort;
import ai.workley.gateway.features.chat.domain.Chat;
import ai.workley.gateway.features.shared.infra.error.InfrastructureErrors;
import ai.workley.gateway.features.chat.domain.event.ChatCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
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

    @Async
    @EventListener
    @Order(0)
    public void handle(ChatCreated e) {
        Chat.Summary summary =
                Chat.Summary.create(e.prompt());

        Set<Chat.Participant> participants =
                Set.of(Chat.Participant.create(e.actor()));

        chatPort.save(Chat.create(e.chatId(), summary, participants))
                .doOnSuccess((Chat chat) ->
                        log.info("Chat created (actor={}, chatId={})",
                                e.actor(), e.chatId())
                )
                .onErrorResume(InfrastructureErrors::isDuplicateKey, error -> {
                    log.warn("Chat already exists (actor={}, chatId={})",
                            e.actor(), e.chatId(), error);
                    return Mono.empty();
                })
                .subscribe();
    }
}
