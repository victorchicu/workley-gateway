package ai.jobbortunity.gateway.chat.application.handler;

import ai.jobbortunity.gateway.chat.application.command.CreateChat;
import ai.jobbortunity.gateway.chat.application.result.CreateChatResult;
import ai.jobbortunity.gateway.chat.domain.model.Message;
import ai.jobbortunity.gateway.chat.domain.event.ChatCreated;
import ai.jobbortunity.gateway.chat.application.error.ApplicationError;
import ai.jobbortunity.gateway.chat.infrastructure.service.IdGenerator;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CreateChatHandler implements CommandHandler<CreateChat, CreateChatResult> {
    private static final Logger log = LoggerFactory.getLogger(CreateChatHandler.class);

    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CreateChatHandler(
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
    public Class<CreateChat> supported() {
        return CreateChat.class;
    }

    @Override
    public Mono<CreateChatResult> handle(String actor, CreateChat command) {
        return Mono.defer(() -> {
            String chatId = randomIdGenerator.generate();

            Message<String> dummy =
                    Message.anonymous(UUID.randomUUID().toString(), chatId, actor, command.prompt());

            ChatCreated chatCreated = new ChatCreated(actor, chatId, command.prompt());

            Mono<CreateChatResult> tx =
                    transactionalOperator.transactional(
                            eventStore.save(actor, chatCreated)
                                    .thenReturn(CreateChatResult.response(chatId, dummy)));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(chatCreated));
        }).onErrorMap(error -> {
            log.error("Oops! Could not create chat.", error);
            return new ApplicationError("Oops! Could not create chat.");
        });
    }
}
