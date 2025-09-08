package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.CommandHandler;
import io.zumely.gateway.resume.application.event.impl.AssistantReplyAddedApplicationEvent;
import io.zumely.gateway.resume.infrastructure.data.EventObject;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class AskAssistantCommandHandler implements CommandHandler<AskAssistantCommand, AskAssistantCommandResult> {
    private final EventStore eventStore;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AskAssistantCommandHandler(
            EventStore eventStore,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Mono<AskAssistantCommandResult> handle(Principal actor, AskAssistantCommand command) {
        AssistantReplyAddedApplicationEvent assistantReplyAddedApplicationEvent
                = new AssistantReplyAddedApplicationEvent(actor, command.chatId(), command.prompt());

        return eventStore.save(actor, assistantReplyAddedApplicationEvent)
                .doOnSuccess((EventObject<AssistantReplyAddedApplicationEvent> eventObject) -> {
                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                })
                .map((EventObject<AssistantReplyAddedApplicationEvent> eventObject) -> new AskAssistantCommandResult());
    }

    @Override
    public Class<AskAssistantCommand> supported() {
        return AskAssistantCommand.class;
    }
}
