package io.zumely.gateway.resume.application.command.handler.impl;

import io.zumely.gateway.resume.application.event.context.ActorApplicationEventPublisher;
import io.zumely.gateway.resume.application.service.ChatIdGenerator;
import io.zumely.gateway.resume.application.command.handler.CommandHandler;
import io.zumely.gateway.resume.application.command.data.CreateChatCommand;
import io.zumely.gateway.resume.application.command.data.CreateChatCommandResult;
import io.zumely.gateway.resume.application.event.CreateChatApplicationEvent;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class CreateResumeCommandHandler implements CommandHandler<CreateChatCommand, CreateChatCommandResult> {
    private final ChatIdGenerator chatIdGenerator;
    private final ConversionService conversionService;
    private final ActorApplicationEventPublisher eventPublisher;

    public CreateResumeCommandHandler(
            ChatIdGenerator chatIdGenerator,
            ConversionService conversionService,
            ActorApplicationEventPublisher eventPublisher
    ) {
        this.chatIdGenerator = chatIdGenerator;
        this.conversionService = conversionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CreateChatCommandResult handle(Principal actor, CreateChatCommand command) {

        CreateChatApplicationEvent createChatApplicationEvent =
                new CreateChatApplicationEvent(chatIdGenerator.generate(), command.prompt());

        eventPublisher.publishEvent(actor, createChatApplicationEvent);

        return toCreateChatResult(createChatApplicationEvent);
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }


    private CreateChatCommandResult toCreateChatResult(CreateChatApplicationEvent event) {
        return conversionService.convert(event, CreateChatCommandResult.class);
    }
}
