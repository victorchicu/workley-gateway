package ai.workley.gateway.features.chat.app.command.handler;

import ai.workley.gateway.features.chat.application.*;
import ai.workley.gateway.features.chat.domain.error.ApplicationError;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.command.CreateChat;
import ai.workley.gateway.features.chat.domain.command.results.CreateChatResult;
import ai.workley.gateway.features.chat.domain.event.ChatCreated;
import ai.workley.gateway.features.chat.infra.component.IdGenerator;
import ai.workley.gateway.features.chat.infra.eventstore.EventStore;
import ai.workley.gateway.features.shared.app.command.CommandHandler;
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
            return new ApplicationError("Oops! Something went wrong, please try again.");
        });
    }
}
