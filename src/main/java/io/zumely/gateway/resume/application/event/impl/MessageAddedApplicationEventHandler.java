package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.CommandDispatcher;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.impl.GenerateReplyCommand;
import io.zumely.gateway.resume.application.command.impl.GenerateReplyCommandResult;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.MessageHistoryRepository;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageAddedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageAddedApplicationEventHandler.class);

    private final CommandDispatcher commandDispatcher;
    private final MessageHistoryRepository messageHistoryRepository;

    public MessageAddedApplicationEventHandler(
            CommandDispatcher commandDispatcher,
            MessageHistoryRepository messageHistoryRepository
    ) {
        this.commandDispatcher = commandDispatcher;
        this.messageHistoryRepository = messageHistoryRepository;
    }

    @EventListener
    public Mono<GenerateReplyCommandResult> handle(MessageAddedApplicationEvent source) {
        MessageObject<String> message =
                MessageObject.create(
                        source.message().id(),
                        source.message().writtenBy(),
                        source.message().chatId(),
                        source.actor().getName(),
                        source.message().createdAt(),
                        source.message().content()
                );
        return messageHistoryRepository.save(message)
                .flatMap((MessageObject<String> messageObject) -> {
                    log.info("Successfully saved {} event: {}",
                            source.getClass().getSimpleName(), source);
                    return dispatchCommand(source);
                })
                .doOnError(error -> {
                    String formatted = "Failed to save %s event: %s"
                            .formatted(source.getClass().getSimpleName(), source);
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Could not save your message.")));
    }

    private Mono<GenerateReplyCommandResult> dispatchCommand(MessageAddedApplicationEvent source) {
        return commandDispatcher
                .dispatch(source.actor(),
                        new GenerateReplyCommand(source.message().content(), source.message().chatId()));
    }
}
