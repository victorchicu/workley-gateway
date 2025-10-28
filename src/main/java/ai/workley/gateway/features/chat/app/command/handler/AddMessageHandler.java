package ai.workley.gateway.features.chat.app.command.handler;

import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.command.AddMessageInput;
import ai.workley.gateway.features.chat.domain.command.AddMessageOutput;
import ai.workley.gateway.features.chat.domain.event.MessageAdded;
import ai.workley.gateway.features.chat.infra.id.IdGenerator;
import ai.workley.gateway.features.chat.infra.eventstore.EventStore;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.ChatRepository;
import ai.workley.gateway.features.shared.app.command.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class AddMessageHandler implements CommandHandler<AddMessageInput, AddMessageOutput> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageHandler.class);

    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final ChatRepository chatRepository;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageHandler(
            EventStore eventStore,
            IdGenerator randomIdGenerator,
            ChatRepository chatRepository,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.randomIdGenerator = randomIdGenerator;
        this.chatRepository = chatRepository;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<AddMessageInput> supported() {
        return AddMessageInput.class;
    }

    @Override
    public Mono<AddMessageOutput> handle(String actor, AddMessageInput command) {
        return Mono.defer(() ->
                chatRepository.findChat(command.chatId(), Set.of(actor))
                        .switchIfEmpty(Mono.error(new ApplicationError("Oops! Chat not found.")))
                        .flatMap(chat -> {
                            Message<String> message =
                                    Message.anonymous(randomIdGenerator.generate(), chat.getChatId(), actor, command.message().content());

                            MessageAdded messageAdded =
                                    new MessageAdded(actor, chat.getChatId(), message);

                            Mono<AddMessageOutput> tx =
                                    transactionalOperator.transactional(
                                            eventStore.save(actor, messageAdded)
                                                    .thenReturn(AddMessageOutput.response(chat.getChatId(), message)));

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
