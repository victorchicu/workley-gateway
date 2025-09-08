package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.CommandHandler;
import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.command.Role;
import io.zumely.gateway.resume.application.event.impl.ChatCreatedApplicationEvent;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.data.EventObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class CreateChatCommandHandler implements CommandHandler<CreateChatCommand, CreateChatCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator chatIdGenerator;
    private final IdGenerator messageIdGenerator;;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CreateChatCommandHandler(
            EventStore eventStore,
            IdGenerator chatIdGenerator,
            IdGenerator messageIdGenerator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.chatIdGenerator = chatIdGenerator;
        this.messageIdGenerator = messageIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private static CreateChatCommandResult toCreateChatCommandResult(EventObject<ChatCreatedApplicationEvent> eventObject) {
        return CreateChatCommandResult.response(
                eventObject.getEventData().chatId(),
                eventObject.getEventData().message());
    }

    @Override
    public Mono<CreateChatCommandResult> handle(Principal actor, CreateChatCommand command) {
        String chatId = chatIdGenerator.generate();

        Message<String> message =
                Message.create(messageIdGenerator.generate(), chatId, actor.getName(), Role.ANONYMOUS, command.prompt());

        ChatCreatedApplicationEvent chatCreatedApplicationEvent =
                new ChatCreatedApplicationEvent(actor, chatId, message);

        return eventStore.save(actor, chatCreatedApplicationEvent)
                .doOnSuccess((EventObject<ChatCreatedApplicationEvent> eventObject) -> {
                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                })
                .map(CreateChatCommandHandler::toCreateChatCommandResult);
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }
}
