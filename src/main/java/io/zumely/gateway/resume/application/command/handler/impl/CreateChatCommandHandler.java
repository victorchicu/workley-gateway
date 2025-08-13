package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.command.data.*;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.event.data.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.EventObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class CreateChatCommandHandler implements CommandHandler<CreateChatCommand, CreateChatCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator chatIdGenerator;
    private final IdGenerator messageIdGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public CreateChatCommandHandler(
            EventStore eventStore,
            IdGenerator chatIdGenerator,
            IdGenerator messageIdGenerator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventStore = eventStore;
        this.chatIdGenerator = chatIdGenerator;
        this.messageIdGenerator = messageIdGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<CreateChatCommandResult> handle(Principal actor, CreateChatCommand command) {
        Message<String> message = Message.valueOf(messageIdGenerator.generate(), actor.getName(), command.prompt());

        CreateChatApplicationEvent createChatApplicationEvent =
                new CreateChatApplicationEvent(actor, chatIdGenerator.generate(), message);

        return eventStore.save(actor, createChatApplicationEvent)
                .doOnSuccess((EventObject<CreateChatApplicationEvent> entity) -> {
                    eventPublisher.publishEvent(entity.getEventData());
                })
                .map((EventObject<CreateChatApplicationEvent> entity) -> {
                    return CreateChatCommandResult.response(
                            entity.getEventData().chatId(), entity.getEventData().message());
                });
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }
}