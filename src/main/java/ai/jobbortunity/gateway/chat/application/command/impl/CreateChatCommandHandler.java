package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.event.impl.CreateChatEvent;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.application.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.UUID;

@Component
public class CreateChatCommandHandler implements CommandHandler<CreateChatCommand, CreateChatCommandResult> {
    private static final Logger log = LoggerFactory.getLogger(CreateChatCommandHandler.class);

    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CreateChatCommandHandler(
            EventStore eventStore,
            IdGenerator randomIdGenerator,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.randomIdGenerator = randomIdGenerator;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<CreateChatCommand> supported() {
        return CreateChatCommand.class;
    }

    @Override
    public Mono<CreateChatCommandResult> handle(String actor, CreateChatCommand command) {
        return Mono.defer(() -> {
            String chatId = randomIdGenerator.generate();

            Message<String> dummy =
                    Message.anonymous(UUID.randomUUID().toString(), chatId, actor, command.prompt());

            CreateChatEvent createChatEvent = new CreateChatEvent(actor, chatId, command.prompt());

            Mono<CreateChatCommandResult> tx =
                    transactionalOperator.transactional(
                            eventStore.save(actor, createChatEvent)
                                    .thenReturn(CreateChatCommandResult.response(chatId, dummy)));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(createChatEvent));
        }).onErrorMap(error -> {
            log.error("Oops! Could not create chat.", error);
            return new ApplicationException("Oops! Could not create chat.");
        });
    }
}