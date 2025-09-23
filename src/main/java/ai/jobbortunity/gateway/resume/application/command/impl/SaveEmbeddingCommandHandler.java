package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.CommandHandler;
import ai.jobbortunity.gateway.resume.application.event.impl.EmbeddingSavedApplicationEvent;
import ai.jobbortunity.gateway.resume.infrastructure.data.EventObject;
import ai.jobbortunity.gateway.resume.infrastructure.eventstore.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class SaveEmbeddingCommandHandler implements CommandHandler<SaveEmbeddingCommand, SaveEmbeddingCommandResult> {
    private final EventStore eventStore;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SaveEmbeddingCommandHandler(EventStore eventStore, ApplicationEventPublisher applicationEventPublisher) {
        this.eventStore = eventStore;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<SaveEmbeddingCommand> supported() {
        return SaveEmbeddingCommand.class;
    }

    @Override
    public Mono<SaveEmbeddingCommandResult> handle(Principal actor, SaveEmbeddingCommand command) {
        EmbeddingSavedApplicationEvent embeddingSavedApplicationEvent =
                new EmbeddingSavedApplicationEvent(actor,
                        command.message());

        return eventStore.save(actor, embeddingSavedApplicationEvent)
                .doOnSuccess((EventObject<EmbeddingSavedApplicationEvent> eventObject) -> {
                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                })
                .map((EventObject<EmbeddingSavedApplicationEvent> eventObject) -> SaveEmbeddingCommandResult.instance());
    }
}
