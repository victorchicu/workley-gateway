package ai.workley.gateway.chat.application.handler;

import ai.workley.gateway.chat.application.command.AddMessage;
import ai.workley.gateway.chat.application.result.AddMessageResult;
import ai.workley.gateway.chat.domain.model.Message;
import ai.workley.gateway.chat.domain.event.MessageAdded;
import ai.workley.gateway.chat.application.error.ApplicationError;
import ai.workley.gateway.chat.domain.model.IdGenerator;
import ai.workley.gateway.chat.infrastructure.persistent.readmodel.repository.ChatReadRepository;
import ai.workley.gateway.chat.infrastructure.persistent.eventstore.EventStore;
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
            log.error("Oops! Could not add your prompt. chatId={}", command.chatId(), error);
            return error instanceof ApplicationError
                    ? error
                    : new ApplicationError("Oops! Could not add your prompt.", error);
        });
    }
}
