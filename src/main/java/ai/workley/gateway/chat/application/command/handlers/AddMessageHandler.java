package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.application.ports.ChatPort;
import ai.workley.gateway.chat.domain.Message;
import ai.workley.gateway.chat.domain.command.AddMessage;
import ai.workley.gateway.chat.domain.payloads.AddMessagePayload;
import ai.workley.gateway.chat.domain.events.MessageAdded;
import ai.workley.gateway.chat.infrastructure.generators.IdGenerator;
import ai.workley.gateway.chat.infrastructure.eventstore.EventStore;
import ai.workley.gateway.chat.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class AddMessageHandler implements CommandHandler<AddMessage, AddMessagePayload> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageHandler.class);

    private final ChatPort chatPort;
    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageHandler(
            ChatPort chatPort,
            EventStore eventStore,
            IdGenerator randomIdGenerator,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.chatPort = chatPort;
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
                chatPort.findChat(command.chatId(), Set.of(actor))
                        .switchIfEmpty(Mono.error(new ApplicationError("Oops! Chat not found.")))
                        .flatMap(chat -> {
                            Message<String> message =
                                    Message.create(randomIdGenerator.generate(), chat.id(), actor, command.message().content());

                            MessageAdded messageAdded =
                                    new MessageAdded(actor, chat.id(), message);

                            Mono<AddMessagePayload> tx =
                                    transactionalOperator.transactional(
                                            eventStore.save(actor, messageAdded)
                                                    .thenReturn(AddMessagePayload.create(chat.id(), message)));

                            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(messageAdded));
                        })
        ).onErrorMap(error -> {
            log.error("Oops! Could not add your message. chatId={}", command.chatId(), error);
            return error instanceof ApplicationError
                    ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
