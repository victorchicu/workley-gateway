package ai.workley.gateway.chat.service;

import ai.workley.gateway.chat.service.EventService;
import ai.workley.gateway.chat.model.ApplicationError;
import ai.workley.gateway.chat.model.Message;
import ai.workley.gateway.chat.model.Role;
import ai.workley.gateway.chat.model.AggregateCommit;
import ai.workley.gateway.chat.model.AggregateTypes;
import ai.workley.gateway.chat.model.ChatAggregate;
import ai.workley.gateway.chat.model.AddMessage;
import ai.workley.gateway.chat.model.Content;
import ai.workley.gateway.chat.model.DomainEvent;
import ai.workley.gateway.chat.model.EventEnvelope;
import ai.workley.gateway.chat.model.AddMessagePayload;
import ai.workley.gateway.chat.model.MessageAdded;
import ai.workley.gateway.chat.service.EventBus;
import ai.workley.gateway.chat.model.ConcurrencyException;
import ai.workley.gateway.chat.service.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class AddMessageHandler implements CommandHandler<AddMessage, AddMessagePayload> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageHandler.class);

    private final EventBus eventBus;
    private final EventService eventService;
    private final TransactionalOperator transactionalOperator;

    public AddMessageHandler(
            EventBus eventBus,
            EventService eventService,
            TransactionalOperator transactionalOperator
    ) {
        this.eventBus = eventBus;
        this.eventService = eventService;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Class<AddMessage> supported() {
        return AddMessage.class;
    }

    @Override
    public Mono<AddMessagePayload> handle(String actor, AddMessage command) {
        return handle(actor, command, null);
    }

    @Override
    public Mono<AddMessagePayload> handle(String actor, AddMessage command, String idempotencyKey) {
        return Mono.defer(() ->
                        eventService.load(AggregateTypes.CHAT, command.chatId())
                                .collectList()
                                .flatMap(history -> replay(actor, command, history)))
                .onErrorMap(this::handleError);
    }


    private Throwable handleError(Throwable error) {
        if (error instanceof ApplicationError) {
            log.error("Application error: {}", error.getMessage(), error);
            return error;
        }
        if (error instanceof ConcurrencyException ce) {
            log.warn("Concurrency conflict occurred", ce);
            return new ApplicationError(
                    "Oops! The chat changed while you were typing. Please try again.", ce
            );
        }
        log.error("Unexpected error occurred", error);
        return new ApplicationError(
                "Oops! Something went wrong. Please try again.", error
        );
    }

    private Mono<AddMessagePayload> replay(String actor, AddMessage command, List<EventEnvelope<DomainEvent>> history) {
        ChatAggregate aggregate;
        try {
            aggregate = ChatAggregate.rehydrate(history);
        } catch (IllegalStateException notFound) {
            log.error("Chat not found (chatId={})", command.chatId());
            return Mono.error(new ApplicationError("Oops! Chat not found."));
        }

        AggregateCommit<MessageAdded> commit;
        try {
            commit =
                    command.message().id() == null
                            ? newAggregate(actor, command, aggregate) : aggregate.addMessage(actor, command.message());
        } catch (IllegalStateException notAllowed) {
            log.error("Oops! You can't add a message to this chat. chatId={}", command.chatId());
            return Mono.error(new ApplicationError("Oops! You can't add a message to this chat."));
        }

        Mono<AddMessagePayload> tx =
                transactionalOperator.transactional(
                        eventService.append(commit.event(), AggregateTypes.CHAT, command.chatId(), commit.version())
                                .thenReturn(AddMessagePayload.ack(aggregate.chatId(), commit.event().message()))
                );

        return tx.doOnSuccess(__ -> eventBus.publishEvent(commit.event()));
    }

    private AggregateCommit<MessageAdded> newAggregate(String actor, AddMessage command, ChatAggregate aggregate) {
        String dummyId =
                UUID.randomUUID().toString();

        Message<? extends Content> message =
                Message.create(dummyId, command.chatId(), actor, Role.ANONYMOUS, Instant.now(), command.message().content());

        return aggregate.addMessage(actor, message);
    }
}
