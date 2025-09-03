package io.zumely.gateway.resume.application.command;

import io.zumely.gateway.resume.application.event.MessageReceivedApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.ChatSessionRepository;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.data.EventObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class SendMessageCommandHandler implements CommandHandler<SendMessageCommand, SendMessageCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator messageIdGenerator;
    private final ChatSessionRepository chatSessionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SendMessageCommandHandler(
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
    public Class<SendMessageCommand> supported() {
        return SendMessageCommand.class;
    }

    @Override
    public Mono<SendMessageCommandResult> handle(Principal actor, SendMessageCommand command) {
        Set<String> participants = Set.of(actor.getName());
        return chatSessionRepository.findChat(command.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationException("Oops. Chat not found.")))
                .flatMap(chatObject -> {
                    Message<String> message =
                            Message.create(messageIdGenerator.generate(), chatObject.getId(), actor.getName(), Role.USER, command.message().content());

                    MessageReceivedApplicationEvent messageReceivedApplicationEvent =
                            new MessageReceivedApplicationEvent(actor, command.chatId(), message);

                    return eventStore.save(actor, messageReceivedApplicationEvent)
                            .doOnSuccess(eventObject -> {
                                applicationEventPublisher.publishEvent(eventObject.getEventData());
                            })
                            .map((EventObject<MessageReceivedApplicationEvent> eventObject) ->
                                    SendMessageCommandResult.response(
                                            eventObject.getEventData().chatId(), eventObject.getEventData().message())
                            );
                });
    }
}