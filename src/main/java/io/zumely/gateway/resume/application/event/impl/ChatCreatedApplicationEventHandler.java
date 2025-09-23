package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.CommandDispatcher;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.impl.AddMessageCommand;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.data.ChatObject;
import io.zumely.gateway.resume.infrastructure.data.MessageObject;
import io.zumely.gateway.resume.infrastructure.data.ParticipantObject;
import io.zumely.gateway.resume.infrastructure.data.SummaryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class ChatCreatedApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatCreatedApplicationEventHandler.class);

    private final CommandDispatcher commandDispatcher;
    private final ChatSessionRepository chatSessionRepository;

    public ChatCreatedApplicationEventHandler(CommandDispatcher commandDispatcher, ChatSessionRepository chatSessionRepository) {
        this.commandDispatcher = commandDispatcher;
        this.chatSessionRepository = chatSessionRepository;
    }

    private static Message<String> toMessage(MessageObject<String> messageObject) {
        return Message.create(
                messageObject.getId(),
                messageObject.getChatId(),
                messageObject.getAuthorId(),
                messageObject.getWrittenBy(),
                messageObject.getCreatedAt(),
                messageObject.getContent()
        );
    }

    private static MessageObject<String> toMessageObject(ChatCreatedApplicationEvent source) {
        return MessageObject.create(
                source.message().id(),
                source.message().writtenBy(),
                source.message().chatId(),
                source.actor().getName(),
                source.message().createdAt(),
                source.message().content()
        );
    }

    @EventListener
    public Mono<Void> handle(ChatCreatedApplicationEvent source) {
        Set<ParticipantObject> participants
                = Set.of(ParticipantObject.create(source.actor().getName()));

        SummaryObject<MessageObject<String>> summary
                = SummaryObject.create(toMessageObject(source));

        return chatSessionRepository.save(ChatObject.create(source.chatId(), summary, participants))
                .flatMap((ChatObject chatObject) -> {
                    log.info("Successfully saved {} event: {}",
                            source.getClass().getSimpleName(), source);
                    Message<String> message = toMessage(chatObject.getSummary().getMessage());
                    return commandDispatcher
                            .dispatch(source.actor(),
                                    new AddMessageCommand(chatObject.getChatId(), message)).then();
                })
                .doOnError(error -> {
                    String formatted = "Failed to save %s event: %s"
                            .formatted(source.getClass().getSimpleName(), source);
                    log.error(formatted, error);
                })
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Could not save your chat.")));
    }
}
