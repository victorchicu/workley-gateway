package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.application.command.Command;
import ai.jobbortunity.gateway.resume.application.command.CommandDispatcher;
import ai.jobbortunity.gateway.resume.application.command.CommandResult;
import ai.jobbortunity.gateway.resume.application.command.Message;
import ai.jobbortunity.gateway.resume.application.command.impl.GenerateReplyCommand;
import ai.jobbortunity.gateway.resume.application.command.impl.GenerateReplyCommandResult;
import ai.jobbortunity.gateway.resume.application.command.impl.SaveEmbeddingCommand;
import ai.jobbortunity.gateway.resume.application.exception.ApplicationException;
import ai.jobbortunity.gateway.resume.infrastructure.MessageHistoryRepository;
import ai.jobbortunity.gateway.resume.infrastructure.data.MessageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

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
    public <T extends CommandResult> Mono<GenerateReplyCommandResult> handle(MessageAddedApplicationEvent source) {
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

                    return dispatchCommand(source.actor(), new SaveEmbeddingCommand(toMessage(messageObject)))
                            .then(this.<GenerateReplyCommand, GenerateReplyCommandResult>dispatchCommand(source.actor(),
                                    new GenerateReplyCommand(source.message().content(), source.message().chatId())));
                })
                .doOnError(error -> {
                    String formatted = "Failed to save %s event: %s"
                            .formatted(source.getClass().getSimpleName(), source);
                    log.error(formatted, error);
                })
                .onErrorMap(error -> new ApplicationException("Oops! Could not save your message."));
    }

    private Message<String> toMessage(MessageObject<String> source) {
        return Message.create(source.getId(), source.getChatId(), source.getAuthorId(), source.getWrittenBy(), source.getCreatedAt(), source.getContent());
    }

    private <T extends Command, R extends CommandResult> Mono<R> dispatchCommand(Principal actor, T source) {
        return commandDispatcher
                .dispatch(actor, source);
    }
}
