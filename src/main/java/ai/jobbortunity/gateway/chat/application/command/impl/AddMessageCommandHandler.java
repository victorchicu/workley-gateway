package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.event.impl.AddMessageEvent;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.ChatSessionRepository;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

@Component
public class AddMessageCommandHandler implements CommandHandler<AddMessageCommand, AddMessageCommandResult> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageCommandHandler.class);

    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final ChatSessionRepository chatSessionRepository;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageCommandHandler(
            EventStore eventStore,
            IdGenerator randomIdGenerator,
            ChatSessionRepository chatSessionRepository,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.randomIdGenerator = randomIdGenerator;
        this.chatSessionRepository = chatSessionRepository;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<AddMessageCommand> supported() {
        return AddMessageCommand.class;
    }

    @Override
    public Mono<AddMessageCommandResult> handle(String actor, AddMessageCommand command) {
        return Mono.defer(() ->
                chatSessionRepository.findChat(command.chatId(), Set.of(actor))
                        .switchIfEmpty(Mono.error(new ApplicationException("Oops! Chat not found.")))
                        .flatMap(chat -> {
                            Message<String> message =
                                    Message.anonymous(randomIdGenerator.generate(), chat.getChatId(), actor, command.message().content());

                            AddMessageEvent addMessageEvent =
                                    new AddMessageEvent(actor, chat.getChatId(), message);

                            Mono<AddMessageCommandResult> tx =
                                    transactionalOperator.transactional(
                                            eventStore.save(actor, addMessageEvent)
                                                    .thenReturn(AddMessageCommandResult.response(chat.getChatId(), message)));

                            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(addMessageEvent));
                        })
        ).onErrorMap(error -> {
            log.error("Oops! Could not add your prompt. chatId={}", command.chatId(), error);
            return error instanceof ApplicationException
                    ? error
                    : new ApplicationException("Oops! Could not add your prompt.", error);
        });
    }
}
