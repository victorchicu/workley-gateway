package ai.workley.gateway.features.chat.app.command.handler;

import ai.workley.gateway.features.chat.domain.error.ApplicationError;
import ai.workley.gateway.features.chat.domain.Message;
import ai.workley.gateway.features.chat.domain.command.AddMessage;
import ai.workley.gateway.features.chat.domain.command.results.AddMessageResult;
import ai.workley.gateway.features.chat.domain.event.MessageAdded;
import ai.workley.gateway.features.chat.infra.component.IdGenerator;
import ai.workley.gateway.features.chat.infra.eventstore.EventStore;
import ai.workley.gateway.features.chat.infra.persistent.ChatReadRepository;
import ai.workley.gateway.features.shared.app.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class AddMessageHandler implements CommandHandler<AddMessage, AddMessageResult> {
    private static final Logger log = LoggerFactory.getLogger(AddMessageHandler.class);

    private final EventStore eventStore;
    private final IdGenerator randomIdGenerator;
    private final ChatReadRepository chatReadRepository;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AddMessageHandler(
            EventStore eventStore,
            IdGenerator randomIdGenerator,
            ChatReadRepository chatReadRepository,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.randomIdGenerator = randomIdGenerator;
        this.chatReadRepository = chatReadRepository;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<AddMessage> supported() {
        return AddMessage.class;
    }

    @Override
    public Mono<AddMessageResult> handle(String actor, AddMessage command) {
        return Mono.defer(() ->
                chatReadRepository.findChat(command.chatId(), Set.of(actor))
                        .switchIfEmpty(Mono.error(new ApplicationError("Oops! Chat not found.")))
                        .flatMap(chat -> {
                            Message<String> message =
                                    Message.anonymous(randomIdGenerator.generate(), chat.getChatId(), actor, command.message().content());

                            MessageAdded messageAdded =
                                    new MessageAdded(actor, chat.getChatId(), message);

                            Mono<AddMessageResult> tx =
                                    transactionalOperator.transactional(
                                            eventStore.save(actor, messageAdded)
                                                    .thenReturn(AddMessageResult.response(chat.getChatId(), message)));

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
