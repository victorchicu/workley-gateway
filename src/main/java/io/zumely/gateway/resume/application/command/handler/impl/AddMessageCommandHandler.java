package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.command.data.AddMessageCommand;
import io.zumely.gateway.resume.application.command.data.AddMessageCommandResult;
import io.zumely.gateway.resume.application.command.data.Message;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.event.data.MessageAddedApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.EventObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class AddMessageCommandHandler implements CommandHandler<AddMessageCommand, AddMessageCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator messageIdGenerator;
    private final ChatSessionRepository chatSessionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageCommandHandler(EventStore eventStore, IdGenerator messageIdGenerator, ChatSessionRepository chatSessionRepository, ApplicationEventPublisher applicationEventPublisher) {
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
        return chatSessionRepository.findChatObjectById(command.chatId())
                .switchIfEmpty(Mono.error(new ApplicationException("Oops. Chat not found.")))
                .flatMap(chatObject -> {
                    MessageAddedApplicationEvent messageAddedApplicationEvent =
                            new MessageAddedApplicationEvent(actor, command.chatId(),
                                    Message.valueOf(messageIdGenerator.generate(), actor.getName(), command.message().content()));

                    return eventStore.save(actor, messageAddedApplicationEvent)
                            .doOnSuccess(this::publish)
                            .map((EventObject<MessageAddedApplicationEvent> entity) ->
                                    AddMessageCommandResult.response(
                                            entity.getEventData().chatId(),
                                            entity.getEventData().message())
                            );
                });
    }


    private void publish(EventObject<MessageAddedApplicationEvent> source) {
        applicationEventPublisher.publishEvent(source.getEventData());
    }
}