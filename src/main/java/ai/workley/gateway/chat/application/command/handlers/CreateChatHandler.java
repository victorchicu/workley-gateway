package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.command.CreateChat;
import ai.workley.gateway.chat.domain.payloads.CreateChatPayload;
import ai.workley.gateway.chat.domain.events.ChatCreated;
import ai.workley.gateway.chat.application.ports.outbound.EventBus;
import ai.workley.gateway.chat.infrastructure.id.IdGenerator;
import ai.workley.gateway.chat.application.ports.outbound.EventStore;
import ai.workley.gateway.chat.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
public class CreateChatHandler implements CommandHandler<CreateChat, CreateChatPayload> {
    private static final Logger log = LoggerFactory.getLogger(CreateChatHandler.class);

    private final EventBus eventBus;
    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final TransactionalOperator transactionalOperator;

    public CreateChatHandler(
            EventBus eventBus,
            EventStore eventStore,
            IdGenerator randomIdGenerator,
            TransactionalOperator transactionalOperator
    ) {
        this.eventBus = eventBus;
        this.eventStore = eventStore;
        this.randomIdGenerator = randomIdGenerator;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Class<CreateChat> supported() {
        return CreateChat.class;
    }

    @Override
    public Mono<CreateChatPayload> handle(String actor, CreateChat command) {
        return Mono.defer(() -> {
            String chatId = randomIdGenerator.generate();

            Message<String> dummy =
                    Message.create(UUID.randomUUID().toString(), chatId, actor, Role.ANONYMOUS, Instant.now(), command.prompt());

            ChatCreated chatCreated = new ChatCreated(actor, chatId, command.prompt());

            Mono<CreateChatPayload> tx =
                    transactionalOperator.transactional(
                            eventStore.append(actor, chatCreated, -1L)
                                    .thenReturn(CreateChatPayload.create(chatId, dummy)));

            return tx.doOnSuccess(__ -> eventBus.publishEvent(chatCreated));
        }).onErrorMap(error -> {
            log.error("Oops! Could not create chat.", error);
            return new ApplicationError("Oops! Could not create chat.");
        });
    }
}
