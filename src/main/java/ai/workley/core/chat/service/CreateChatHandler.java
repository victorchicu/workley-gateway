package ai.workley.core.chat.service;

import ai.workley.core.chat.model.ApplicationError;
import ai.workley.core.chat.model.Message;
import ai.workley.core.chat.model.Role;
import ai.workley.core.chat.model.AggregateTypes;
import ai.workley.core.chat.model.CreateChat;
import ai.workley.core.chat.model.ReplyChunk;
import ai.workley.core.chat.model.Idempotency;
import ai.workley.core.chat.model.IdempotencyState;
import ai.workley.core.chat.model.CreateChatPayload;
import ai.workley.core.chat.model.ChatCreated;
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
    private final EventService eventService;
    private final IdGenerator randomIdGenerator;
    private final IdempotencyGuard idempotencyGuard;
    private final TransactionalOperator transactionalOperator;

    public CreateChatHandler(
            EventBus eventBus,
            EventService eventService,
            IdGenerator randomIdGenerator,
            IdempotencyGuard idempotencyGuard,
            TransactionalOperator transactionalOperator
    ) {
        this.eventBus = eventBus;
        this.eventService = eventService;
        this.randomIdGenerator = randomIdGenerator;
        this.idempotencyGuard = idempotencyGuard;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Class<CreateChat> supported() {
        return CreateChat.class;
    }

    @Override
    public Mono<CreateChatPayload> handle(String actor, CreateChat command) {
        return handle(actor, command, null);
    }

    @Override
    public Mono<CreateChatPayload> handle(String actor, CreateChat command, String idempotencyKey) {
        return Mono.defer(() -> {
            String resourceId = randomIdGenerator.generate();

            Mono<Idempotency> idempotency =
                    idempotencyKey != null
                            ? idempotencyGuard.ensureIdempotency(idempotencyKey, resourceId)
                            : Mono.just(new Idempotency().setResourceId(resourceId).setState(IdempotencyState.PROCESSING));

            return idempotency.flatMap(idem -> {
                String chatId = idem.getResourceId();
                if (idempotencyKey != null && idem.getState() == IdempotencyState.COMPLETED) {
                    Message<ReplyChunk> dummy =
                            Message.create(UUID.randomUUID().toString(), chatId, actor, Role.ANONYMOUS, Instant.now(), new ReplyChunk(command.prompt()));
                    return Mono.just(CreateChatPayload.ack(chatId, dummy));
                }

                Message<ReplyChunk> dummy =
                        Message.create(UUID.randomUUID().toString(), chatId, actor, Role.ANONYMOUS, Instant.now(), new ReplyChunk(command.prompt()));

                ChatCreated chatCreated = new ChatCreated(actor, chatId, command.prompt());

                Mono<CreateChatPayload> tx =
                        transactionalOperator.transactional(
                                eventService.append(chatCreated, AggregateTypes.CHAT, chatId, -1L)
                                        .then(idempotencyGuard.markIdempotentCompleted(idempotencyKey, chatId))
                                        .thenReturn(CreateChatPayload.ack(chatId, dummy))
                        );

                return tx.doOnSuccess(payload -> eventBus.publishEvent(chatCreated));
            });
        }).onErrorMap(error -> {
            log.error("Oops! Could not create chat.", error);
            return new ApplicationError("Oops! Could not create chat.");
        });
    }
}
