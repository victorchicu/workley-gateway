package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import ai.jobbortunity.gateway.chat.application.event.impl.SaveEmbeddingEvent;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class SaveEmbeddingCommandHandler implements CommandHandler<SaveEmbeddingCommand, SaveEmbeddingCommandResult> {
    private static final Logger log = LoggerFactory.getLogger(SaveEmbeddingCommandHandler.class);

    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SaveEmbeddingCommandHandler(
            EventStore eventStore,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<SaveEmbeddingCommand> supported() {
        return SaveEmbeddingCommand.class;
    }

    @Override
    public Mono<SaveEmbeddingCommandResult> handle(String actor, SaveEmbeddingCommand command) {
        return Mono.defer(() -> {
            SaveEmbeddingEvent saveEmbeddingEvent =
                    new SaveEmbeddingEvent(actor, command.text(), Collections.emptyMap());

            Mono<SaveEmbeddingCommandResult> tx = transactionalOperator.transactional(
                    eventStore.save(actor, saveEmbeddingEvent)
                            .thenReturn(SaveEmbeddingCommandResult.empty())
            );

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(saveEmbeddingEvent));
        }).onErrorMap(error -> {
            log.error("Oops! Could not save embedding. text={}", command.text(), error);
            return (error instanceof ApplicationException) ? error
                    : new ApplicationException("Oops! Something went wrong.", error);
        });
    }
}
