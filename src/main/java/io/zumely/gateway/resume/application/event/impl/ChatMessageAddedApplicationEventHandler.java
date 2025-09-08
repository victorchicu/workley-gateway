package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.CommandDispatcher;
import io.zumely.gateway.resume.application.command.impl.AskAssistantCommand;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.MessageHistoryRepository;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ChatMessageAddedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageAddedApplicationEventHandler.class);

    private final CommandDispatcher commandDispatcher;
    private final MessageHistoryRepository messageHistoryRepository;

    public ChatMessageAddedApplicationEventHandler(
            CommandDispatcher commandDispatcher,
            MessageHistoryRepository messageHistoryRepository
    ) {
        this.commandDispatcher = commandDispatcher;
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @EventListener
    public Mono<Void> handle(ChatMessageAddedApplicationEvent source) {
        MessageObject<String> message =
                MessageObject.create(
                        source.message().id(),
                        source.message().writtenBy(),
                        source.message().chatId(),
                        source.actor().getName(), source.message().createdAt(),
                        source.message().content()
                );
        return messageHistoryRepository.save(message)
                .flatMap((MessageObject<String> messageObject) -> {
                    log.info("Saved {} event for authorId {}",
                            source.getClass().getSimpleName(), source.actor().getName());
                    return commandDispatcher
                            .dispatch(source.actor(),
                                    new AskAssistantCommand(messageObject.getContent(), messageObject.getChatId())).then();
                })
                .doOnError(error -> {
                    String formatted = "Oops! Something went wrong while saving event %s for authorId %s"
                            .formatted(source.getClass().getSimpleName(), source.actor().getName());
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Something went wrong while saving message.")));
    }
}
