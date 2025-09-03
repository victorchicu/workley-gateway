package io.zumely.gateway.resume.application.event;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.data.ChatObject;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import io.zumely.gateway.resume.infrastructure.data.ParticipantObject;
import io.zumely.gateway.resume.infrastructure.data.SummaryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class CreateChatApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateChatApplicationEventHandler.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateChatApplicationEventHandler(
            ChatSessionRepository chatSessionRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.chatSessionRepository = chatSessionRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public Mono<ChatObject> handle(CreateChatApplicationEvent source) {
        Set<ParticipantObject> participants = Set.of(ParticipantObject.create(source.actor().getName()));
        SummaryObject<MessageObject<String>> summary = SummaryObject.create(toMessageObject(source));
        return chatSessionRepository.save(ChatObject.create(source.chatId(), summary, participants))
                .doOnSuccess((ChatObject chatObject) -> {
                    log.info("Saved {} event for author {}", source.getClass().getSimpleName(), source.actor().getName());
                    MessageObject<String> messageObject = chatObject.getSummary().getMessage();
                    Message<String> message =
                            Message.reply(
                                    messageObject.getId(), messageObject.getChatId(),
                                    messageObject.getAuthor(), messageObject.getRole(),
                                    messageObject.getCreatedAt(), messageObject.getContent());

                    eventPublisher.publishEvent(
                            new MessageReceivedApplicationEvent(source.actor(), chatObject.getId(), message));

                })
                .doOnError(error -> {
                    String formatted = "Oops! Something went wrong while saving event %s for author %s"
                            .formatted(source.getClass().getSimpleName(), source.actor().getName());
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Chat not saved.")));
    }

    private static MessageObject<String> toMessageObject(CreateChatApplicationEvent source) {
        return MessageObject.create(
                source.message().id(),
                source.message().role(),
                source.message().chatId(),
                source.message().createdAt(),
                source.message().content(),
                source.actor().getName()
        );
    }
}