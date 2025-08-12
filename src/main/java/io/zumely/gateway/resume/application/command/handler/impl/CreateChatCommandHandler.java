package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.command.data.*;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.event.data.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.service.IdGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
public class CreateChatCommandHandler implements CommandHandler<CreateChatCommand, CreateChatCommandResult> {
    private final IdGenerator chatIdGenerator;
    private final IdGenerator messageIdGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public CreateChatCommandHandler(
            IdGenerator chatIdGenerator,
            IdGenerator messageIdGenerator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.chatIdGenerator = chatIdGenerator;
        this.messageIdGenerator = messageIdGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateChatCommandResult handle(Principal actor, CreateChatCommand command) {

        CreateChatApplicationEvent createChatApplicationEvent =
                new CreateChatApplicationEvent(
                        actor,
                        chatIdGenerator.generate(),
                        new Message<>(messageIdGenerator.generate(), RoleType.USER, command.prompt()));

        eventPublisher.publishEvent(createChatApplicationEvent);

        return new CreateChatCommandResult(
                createChatApplicationEvent.chatId(), createChatApplicationEvent.message());
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }
}
