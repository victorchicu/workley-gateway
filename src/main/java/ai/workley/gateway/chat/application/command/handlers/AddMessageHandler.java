package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.aggregations.AggregateCommit;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import ai.workley.gateway.chat.domain.aggregations.ChatAggregate;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.domain.payloads.AddMessagePayload;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import ai.workley.gateway.chat.infrastructure.exceptions.ConcurrencyException;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
import ai.workley.gateway.chat.infrastructure.eventstore.EventStore;
import ai.workley.gateway.chat.application.command.CommandHandler;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AddMessageHandler implements CommandHandler<AddMessage, AddMessagePayload> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageHandler.class);

    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageHandler(
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
    public Class<AddMessage> supported() {
        return AddMessage.class;
    }

    @Override
    public Mono<AddMessagePayload> handle(String actor, AddMessage command) {
        return Mono.defer(() ->
                        eventStore.load(AggregateTypes.CHAT, command.chatId())
                                .collectList()
                                .flatMap(history -> reassembleEvent(actor, command, history)))
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

    private Mono<AddMessagePayload> reassembleEvent(String actor, AddMessage command, List<EventDocument<DomainEvent>> history) {
        ChatAggregate aggregate;
        try {
            aggregate = ChatAggregate.rehydrate(history);
        } catch (IllegalStateException notFound) {
            return Mono.error(new ApplicationError("Oops! Chat not found."));
        }

        AggregateCommit<MessageAdded> commit;
        try {
            commit = aggregate.appendMessage(actor, randomIdGenerator.generate(), command.message().content());
        } catch (IllegalStateException notAllowed) {
            return Mono.error(new ApplicationError("Oops! You are not allowed to post in this chat."));
        }

        Mono<AddMessagePayload> tx =
                transactionalOperator.transactional(
                        eventStore.append(actor, commit.event(), commit.expectedVersion())
                                .thenReturn(AddMessagePayload.create(aggregate.id(), commit.event().message()))
                );

        return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(commit.event()));
    }
}
