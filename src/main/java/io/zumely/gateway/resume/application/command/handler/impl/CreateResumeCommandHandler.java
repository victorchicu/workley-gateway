package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.service.ChatIdGenerator;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.command.impl.CreateChatCommand;
import io.zumely.gateway.resume.application.command.result.impl.CreateChatResult;
import io.zumely.gateway.resume.application.event.impl.CreateChatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class CreateResumeCommandHandler implements CommandHandler<CreateChatCommand, CreateChatResult> {
    private final ChatIdGenerator chatIdGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CreateResumeCommandHandler(
            ChatIdGenerator chatIdGenerator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.chatIdGenerator = chatIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public CreateChatResult handle(Principal principal, CreateChatCommand command) {

        CreateChatEvent createChatEvent =
                new CreateChatEvent(
                        principal,
                        chatIdGenerator.generate(),
                        command.prompt()
                );

        applicationEventPublisher.publishEvent(createChatEvent);

        return CreateChatResult.firstReply(createChatEvent);
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }
}