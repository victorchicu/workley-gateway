package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.CommandHandler;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.Role;
import io.zumely.gateway.resume.application.event.impl.MessageAddedApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.data.ChatObject;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.data.EventObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class AddMessageCommandHandler implements CommandHandler<AddMessageCommand, AddMessageCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator messageIdGenerator;
    private final ChatSessionRepository chatSessionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageCommandHandler(
            EventStore eventStore,
            IdGenerator messageIdGenerator,
            ChatSessionRepository chatSessionRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.messageIdGenerator = messageIdGenerator;
        this.chatSessionRepository = chatSessionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<AddMessageCommand> supported() {
        return AddMessageCommand.class;
    }

    @Override
    public Mono<AddMessageCommandResult> handle(Principal actor, AddMessageCommand command) {
        Set<String> participants = Set.of(actor.getName());
        return chatSessionRepository.findChat(command.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationException("Oops. Chat not found.")))
                .flatMap((ChatObject chatObject) -> {
                    Message<String> message =
                            Message.create(messageIdGenerator.generate(), chatObject.getChatId(), actor.getName(), Role.ANONYMOUS, command.message().content());

                    MessageAddedApplicationEvent messageAddedApplicationEvent =
                            new MessageAddedApplicationEvent(actor, command.chatId(), message);

                    return eventStore.save(actor, messageAddedApplicationEvent)
                            .doOnSuccess((EventObject<MessageAddedApplicationEvent> eventObject) -> {
                                applicationEventPublisher.publishEvent(eventObject.getEventData());
                            })
                            .map((EventObject<MessageAddedApplicationEvent> eventObject) ->
                                    AddMessageCommandResult.response(
                                            eventObject.getEventData().chatId(), eventObject.getEventData().message())
                            );
                });
    }
}
