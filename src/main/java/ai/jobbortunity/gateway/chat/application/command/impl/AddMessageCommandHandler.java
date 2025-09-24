package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.command.Role;
import ai.jobbortunity.gateway.chat.application.event.impl.AddMessageEvent;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.ChatSessionRepository;
import ai.jobbortunity.gateway.chat.infrastructure.data.ChatObject;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import ai.jobbortunity.gateway.chat.infrastructure.data.EventObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
public class AddMessageCommandHandler implements CommandHandler<AddMessageCommand, AddMessageCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator messageIdGenerator;
    private final ChatSessionRepository chatSessionRepository;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageCommandHandler(
            EventStore eventStore,
            IdGenerator messageIdGenerator,
            ChatSessionRepository chatSessionRepository,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.messageIdGenerator = messageIdGenerator;
        this.chatSessionRepository = chatSessionRepository;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<AddMessageCommand> supported() {
        return AddMessageCommand.class;
    }

    @Override
    public Mono<AddMessageCommandResult> handle(Principal actor, AddMessageCommand command) {
        Set<String> participants = Set.of(actor.getName());

        return chatSessionRepository.findChat(command.chatId(), participants)
                .switchIfEmpty(Mono.error(new ApplicationException("Oops! Chat not found.")))
                .flatMap((ChatObject chatObject) -> {
                    Message<String> message =
                            Message.create(messageIdGenerator.generate(), chatObject.getChatId(), actor.getName(), Role.ANONYMOUS, command.message().content());

                    AddMessageEvent addMessageEvent =
                            new AddMessageEvent(
                                    actor, chatObject.getChatId(), message);

                    return eventStore.save(actor, addMessageEvent)
                            .map((EventObject<AddMessageEvent> eventObject) -> {
                                applicationEventPublisher.publishEvent(eventObject.getEventData());
                                return AddMessageCommandResult.response(eventObject.getEventData().chatId(), message);
                            });
                })
                .as(transactionalOperator::transactional)
                .onErrorMap(error -> new ApplicationException("Oops! Could not add your message.", error));
    }
}
