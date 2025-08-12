package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.command.data.SendMessageCommand;
import io.zumely.gateway.resume.application.command.data.SendMessageCommandResult;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.event.data.MessageAddedApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class SendMessageCommandHandler implements CommandHandler<SendMessageCommand, SendMessageCommandResult> {
    private final ApplicationEventPublisher eventPublisher;

    public SendMessageCommandHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SendMessageCommandResult handle(Principal actor, SendMessageCommand command) {
        MessageAddedApplicationEvent messageAddedApplicationEvent =
                new MessageAddedApplicationEvent(actor.getName(), command.chatId(), command.message());

        eventPublisher.publishEvent(messageAddedApplicationEvent);

        return SendMessageCommandResult.response(
                messageAddedApplicationEvent.chatId(),
                messageAddedApplicationEvent.message()
        );
    }

    @Override
    public Class<SendMessageCommand> supported() {
        return SendMessageCommand.class;
    }
}
