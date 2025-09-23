package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.CommandHandler;
import ai.jobbortunity.gateway.resume.application.event.impl.ReplyGeneratedApplicationEvent;
import ai.jobbortunity.gateway.resume.infrastructure.data.EventObject;
import ai.jobbortunity.gateway.resume.infrastructure.eventstore.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class GenerateReplyCommandHandler implements CommandHandler<GenerateReplyCommand, GenerateReplyCommandResult> {
    private final EventStore eventStore;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GenerateReplyCommandHandler(
            EventStore eventStore,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Mono<GenerateReplyCommandResult> handle(Principal actor, GenerateReplyCommand command) {
        ReplyGeneratedApplicationEvent replyGeneratedApplicationEvent
                = new ReplyGeneratedApplicationEvent(actor, command.chatId(), command.prompt());

        return eventStore.save(actor, replyGeneratedApplicationEvent)
                .doOnSuccess((EventObject<ReplyGeneratedApplicationEvent> eventObject) -> {
                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                })
                .map((EventObject<ReplyGeneratedApplicationEvent> eventObject) -> new GenerateReplyCommandResult());
    }

    @Override
    public Class<GenerateReplyCommand> supported() {
        return GenerateReplyCommand.class;
    }
}
