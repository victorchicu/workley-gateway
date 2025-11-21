package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.eventstore.EventService;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.application.idempotency.IdempotencyService;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import ai.workley.gateway.chat.domain.command.CreateChat;
import ai.workley.gateway.chat.domain.content.TextContent;
import ai.workley.gateway.chat.domain.idempotency.Idempotency;
import ai.workley.gateway.chat.domain.payloads.CreateChatPayload;
import ai.workley.gateway.chat.domain.events.ChatCreated;
import ai.workley.gateway.chat.application.ports.outbound.bus.EventBus;
import ai.workley.gateway.chat.infrastructure.id.IdGenerator;
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
    private final EventService eventService;
    private final IdGenerator randomIdGenerator;
    private final IdempotencyService idempotencyService;
    private final TransactionalOperator transactionalOperator;

    public CreateChatHandler(
            EventBus eventBus,
            EventService eventService,
            IdGenerator randomIdGenerator,
            IdempotencyService idempotencyService,
            TransactionalOperator transactionalOperator
    ) {
        this.eventBus = eventBus;
        this.eventService = eventService;
        this.randomIdGenerator = randomIdGenerator;
        this.idempotencyService = idempotencyService;
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
            String chatId = randomIdGenerator.generate();

            Message<TextContent> dummy =
                    Message.create(UUID.randomUUID().toString(), chatId, actor, Role.ANONYMOUS, Instant.now(),
                            new TextContent(command.prompt()));

            ChatCreated chatCreated = new ChatCreated(actor, chatId, command.prompt());

            Mono<CreateChatPayload> tx =
                    transactionalOperator.transactional(
                            ensureIdempotency(idempotencyKey, chatId)
                                    .then(eventService.append(chatCreated, AggregateTypes.CHAT, chatId, -1L))
                                    .then(markIdempotentCompleted(idempotencyKey, chatId))
                                    .thenReturn(CreateChatPayload.ack(chatId, dummy))
                    );

            return tx.flatMap(payload ->
                    Mono.fromRunnable(() -> eventBus.publishEvent(chatCreated))
                            .thenReturn(payload)
            );
        }).onErrorMap(error -> {
            log.error("Oops! Could not create chat.", error);
            return new ApplicationError("Oops! Could not create chat.");
        });
    }

    private Mono<Idempotency> ensureIdempotency(String key, String chatId) {
        if (key == null) return Mono.empty();
        return idempotencyService.ensureIdempotency(key);
    }

    private Mono<Idempotency> markIdempotentCompleted(String key, String chatId) {
        if (key == null) return Mono.empty();
        return idempotencyService.markIdempotentCompleted(key);
    }
}
