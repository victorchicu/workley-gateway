package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.command.CommandHandler;
import ai.workley.gateway.chat.application.idempotency.IdempotencyGuard;
import ai.workley.gateway.chat.application.eventstore.EventService;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import ai.workley.gateway.chat.domain.command.CreateChat;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.idempotency.Idempotency;
import ai.workley.gateway.chat.domain.idempotency.IdempotencyState;
import ai.workley.gateway.chat.domain.payloads.CreateChatPayload;
import ai.workley.gateway.chat.domain.events.ChatCreated;
import ai.workley.gateway.chat.application.ports.outbound.bus.EventBus;
import ai.workley.gateway.chat.infrastructure.id.IdGenerator;
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
                    Message<TextContent> dummy =
                            Message.create(UUID.randomUUID().toString(), chatId, actor, Role.ANONYMOUS, Instant.now(), new TextContent(command.prompt()));
                    return Mono.just(CreateChatPayload.ack(chatId, dummy));
                }

                Message<TextContent> dummy =
                        Message.create(UUID.randomUUID().toString(), chatId, actor, Role.ANONYMOUS, Instant.now(), new TextContent(command.prompt()));

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