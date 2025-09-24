package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.CommandHandler;
import ai.jobbortunity.gateway.resume.application.event.impl.SaveEmbeddingEvent;
import ai.jobbortunity.gateway.resume.application.exception.ApplicationException;
import ai.jobbortunity.gateway.resume.infrastructure.data.EventObject;
import ai.jobbortunity.gateway.resume.infrastructure.eventstore.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class SaveEmbeddingCommandHandler implements CommandHandler<SaveEmbeddingCommand, SaveEmbeddingCommandResult> {
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
    public Mono<SaveEmbeddingCommandResult> handle(Principal actor, SaveEmbeddingCommand command) {
        SaveEmbeddingEvent saveEmbeddingEvent =
                new SaveEmbeddingEvent(actor.getName(), command.chatId(), command.message());

        return Mono.defer(() ->
                        eventStore.save(actor, saveEmbeddingEvent)
                                .map((EventObject<SaveEmbeddingEvent> eventObject) -> {
                                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                                    return SaveEmbeddingCommandResult.empty();
                                })
                )
                .as(transactionalOperator::transactional)
                .onErrorMap(error -> new ApplicationException("Oops! Could not save your message."));


    }
}
