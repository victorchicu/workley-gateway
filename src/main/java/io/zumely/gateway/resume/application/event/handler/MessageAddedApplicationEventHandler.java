package io.zumely.gateway.resume.application.event.handler;

import io.zumely.gateway.resume.application.event.data.MessageAddedApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.eventstore.MessageHistoryRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageAddedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageAddedApplicationEventHandler.class);

    private final MessageHistoryRepository messageHistoryRepository;

    public MessageAddedApplicationEventHandler(MessageHistoryRepository messageHistoryRepository) {
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @EventListener
    public Mono<MessageObject<String>> handle(MessageAddedApplicationEvent source) {
        MessageObject<String> message = new MessageObject<String>()
                .setId(source.message().id()).setOwner(source.actor().getName()).setChatId(source.chatId()).setContent(source.message().content());

        return messageHistoryRepository.save(message)
                .doOnSuccess((MessageObject<?> event) -> {
                    log.info("Saved {} event for actor {}",
                            source.getClass().getSimpleName(), source.actor().getName());
                })
                .doOnError(error -> {
                    String formatted = "Oops! Something went wrong while saving event %s for actor %s"
                            .formatted(source.getClass().getSimpleName(), source.actor().getName());
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Something went wrong while saving message.")));
    }
}