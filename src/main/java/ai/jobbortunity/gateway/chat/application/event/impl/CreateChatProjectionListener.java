package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.infrastructure.ChatSessionRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.ChatObject;
import ai.jobbortunity.gateway.chat.infrastructure.data.MessageObject;
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
public class CreateChatProjectionListener {
    private static final Logger log = LoggerFactory.getLogger(CreateChatProjectionListener.class);

    private final ChatSessionRepository chatSessionRepository;

    public CreateChatProjectionListener(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    private static Message<String> toMessage(MessageObject<String> messageObject) {
        return Message.create(
                messageObject.getId(),
                messageObject.getChatId(),
                messageObject.getAuthorId(),
                messageObject.getRole(),
                messageObject.getCreatedAt(),
                messageObject.getContent()
        );
    }

    private static MessageObject<String> toMessageObject(CreateChatEvent source) {
        return MessageObject.create(
                source.message().id(),
                source.message().role(),
                source.message().chatId(),
                source.actor().getName(),
                source.message().createdAt(),
                source.message().content()
        );
    }

    @EventListener
    @Order(0)
    public Mono<Void> on(CreateChatEvent e) {
        SummaryObject<MessageObject<String>> summary = SummaryObject.create(toMessageObject(e));
        return chatSessionRepository.save(ChatObject.create(e.chatId(), summary, Set.of(ParticipantObject.create(e.actor().getName()))))
                .map((ChatObject chatObject) -> {
                    log.info("Chat created: {}", e);
                    return toMessage(chatObject.getSummary().getMessage());
                })
                .doOnError(error -> {
                    String formatted = "Chat not created: %s".formatted(e);
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.empty())
                .then();
    }
}
