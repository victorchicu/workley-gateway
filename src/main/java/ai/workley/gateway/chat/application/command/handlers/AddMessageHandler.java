package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.Role;
import ai.workley.gateway.chat.domain.aggregations.AggregateCommit;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import ai.workley.gateway.chat.domain.aggregations.ChatAggregate;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.domain.payloads.AddMessagePayload;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import ai.workley.gateway.chat.infrastructure.exceptions.ConcurrencyException;
import ai.workley.gateway.chat.infrastructure.eventstore.EventStore;
import ai.workley.gateway.chat.application.command.CommandHandler;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class AddMessageHandler implements CommandHandler<AddMessage, AddMessagePayload> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageHandler.class);

    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageHandler(
            EventStore eventStore,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<AddMessage> supported() {
        return AddMessage.class;
    }

    @Override
    public Mono<AddMessagePayload> handle(String actor, AddMessage command) {
        return Mono.defer(() ->
                        eventStore.load(AggregateTypes.CHAT, command.chatId())
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

    private Mono<AddMessagePayload> replay(String actor, AddMessage command, List<EventDocument<DomainEvent>> history) {
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
                        eventStore.append(actor, commit.event(), commit.version())
                                .thenReturn(AddMessagePayload.create(aggregate.chatId(), commit.event().message()))
                );

        return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(commit.event()));
    }

    private AggregateCommit<MessageAdded> newAggregate(String actor, AddMessage command, ChatAggregate aggregate) {
        String dummyId =
                UUID.randomUUID().toString();

        Message<String> message =
                Message.create(dummyId, command.chatId(), actor, Role.ANONYMOUS, Instant.now(), command.message().content());

        return aggregate.addMessage(actor, message);
    }
}
