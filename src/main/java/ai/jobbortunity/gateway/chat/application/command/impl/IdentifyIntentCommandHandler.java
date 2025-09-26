package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import ai.jobbortunity.gateway.chat.application.event.impl.IdentifyIntentEvent;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class IdentifyIntentCommandHandler implements CommandHandler<IdentifyIntentCommand, IdentifyIntentCommandResult> {
    private static final Logger log = LoggerFactory.getLogger(IdentifyIntentCommandHandler.class);

    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public IdentifyIntentCommandHandler(
            EventStore eventStore,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<IdentifyIntentCommand> supported() {
        return IdentifyIntentCommand.class;
    }

    @Override
    public Mono<IdentifyIntentCommandResult> handle(String actor, IdentifyIntentCommand command) {
        return Mono.defer(() -> {
            IdentifyIntentEvent identifyIntentEvent = new IdentifyIntentEvent(actor, command.chatId(), command.prompt());

            Mono<IdentifyIntentCommandResult> tx =
                    transactionalOperator.transactional(
                            eventStore.save(actor, identifyIntentEvent)
                                    .thenReturn(IdentifyIntentCommandResult.empty()));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(identifyIntentEvent));
        }).onErrorMap(error -> {
            log.error("Oops! Could not identify intent", error);
            return new ApplicationException("Oops! Could not identify intent.");
        });
    }
}
