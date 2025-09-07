package io.zumely.gateway.resume.application.command;

import io.zumely.gateway.resume.application.event.ChatCreatedApplicationEvent;
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
    private final IdGenerator messageIdGenerator;
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
                .map((EventObject<ChatCreatedApplicationEvent> eventObject) -> {
                    return CreateChatCommandResult.reply(
                            eventObject.getEventData().chatId(),
                            eventObject.getEventData().message());
                });
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }
}