package io.zumely.gateway.resume.application.event;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.data.ChatObject;
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

    private final IdGenerator messageIdGenerator;
    private final ChatSessionRepository chatSessionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateChatApplicationEventHandler(IdGenerator messageIdGenerator, ChatSessionRepository chatSessionRepository, ApplicationEventPublisher eventPublisher) {
        this.messageIdGenerator = messageIdGenerator;
        this.chatSessionRepository = chatSessionRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public Mono<ChatObject> handle(CreateChatApplicationEvent source) {
        SummaryObject<String> summary =
                SummaryObject.create(source.actor().getName(),
                        source.message().content());

        Set<ParticipantObject> participants =
                Set.of(ParticipantObject.create(source.actor().getName()));

        return chatSessionRepository.save(ChatObject.create(source.chatId(), summary, participants))
                .doOnSuccess((ChatObject chatObject) -> {
                    log.info("Saved {} event for actor {}", source.getClass().getSimpleName(), source.actor().getName());

                    Message<String> message =
                            Message.create(messageIdGenerator.generate(),
                                    chatObject.getId(), chatObject.getSummary().getContent());

                    eventPublisher.publishEvent(
                            new MessageAddedApplicationEvent(source.actor(), chatObject.getId(), message));

                })
                .doOnError(error -> {
                    String formatted = "Oops! Something went wrong while saving event %s for actor %s"
                            .formatted(source.getClass().getSimpleName(), source.actor().getName());
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Chat not saved.")));
    }
}