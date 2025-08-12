package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.command.data.CreateChatCommand;
import io.zumely.gateway.resume.application.command.data.CreateChatCommandResult;
import io.zumely.gateway.resume.application.command.data.Message;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.event.data.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.service.ChatIdGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class CreateChatCommandHandler implements CommandHandler<CreateChatCommand, CreateChatCommandResult> {
    private final ChatIdGenerator chatIdGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public CreateChatCommandHandler(
            ChatIdGenerator chatIdGenerator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.chatIdGenerator = chatIdGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateChatCommandResult handle(Principal actor, CreateChatCommand command) {

        CreateChatApplicationEvent createChatApplicationEvent =
                new CreateChatApplicationEvent(actor.getName(), chatIdGenerator.generate(), command.prompt());

        eventPublisher.publishEvent(createChatApplicationEvent);

        return new CreateChatCommandResult(
                createChatApplicationEvent.chatId(),
                new Message<>(createChatApplicationEvent.prompt().text())
        );
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }
}