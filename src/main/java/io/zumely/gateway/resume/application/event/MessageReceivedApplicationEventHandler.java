package io.zumely.gateway.resume.application.event;

import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.MessageHistoryRepository;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageReceivedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageReceivedApplicationEventHandler.class);

    private final MessageHistoryRepository messageHistoryRepository;

    public MessageReceivedApplicationEventHandler(MessageHistoryRepository messageHistoryRepository) {
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @EventListener
    public Mono<MessageObject<String>> handle(MessageReceivedApplicationEvent source) {
        MessageObject<String> message =
                MessageObject.create(
                        source.message().id(),
                        source.message().role(),
                        source.message().chatId(),
                        source.message().createdAt(),
                        source.message().content(),
                        source.actor().getName()
                );

        return messageHistoryRepository.save(message)
                .doOnSuccess((MessageObject<?> event) -> {
                    log.info("Saved {} event for author {}",
                            source.getClass().getSimpleName(), source.actor().getName());
                })
                .doOnError(error -> {
                    String formatted = "Oops! Something went wrong while saving event %s for author %s"
                            .formatted(source.getClass().getSimpleName(), source.actor().getName());
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Something went wrong while saving message.")));
    }
}