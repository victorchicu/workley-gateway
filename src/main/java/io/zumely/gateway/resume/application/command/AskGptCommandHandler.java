package io.zumely.gateway.resume.application.command;

import io.zumely.gateway.resume.application.event.GptExecutedApplicationEvent;
import io.zumely.gateway.resume.application.service.IdGenerator;
import io.zumely.gateway.resume.infrastructure.data.EventObject;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class AskGptCommandHandler implements CommandHandler<AskGptCommand, AskGptCommandResult> {
    private final EventStore eventStore;
    private final IdGenerator messageIdGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AskGptCommandHandler(EventStore eventStore, IdGenerator messageIdGenerator, ApplicationEventPublisher applicationEventPublisher) {
        this.eventStore = eventStore;
        this.messageIdGenerator = messageIdGenerator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Mono<AskGptCommandResult> handle(Principal actor, AskGptCommand command) {
        GptExecutedApplicationEvent gptExecutedApplicationEvent = new GptExecutedApplicationEvent(actor, command.chatId(), command.prompt());

        return eventStore.save(actor, gptExecutedApplicationEvent)
                .doOnSuccess((EventObject<GptExecutedApplicationEvent> eventObject) -> {
                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                })
                .map((EventObject<GptExecutedApplicationEvent> eventObject) -> {
                    return CreateChatCommandResult.reply(
                            eventObject.getEventData().chatId(),
                            Message.create(""));
                });
    }

    @Override
    public Class<AskGptCommand> supported() {
        return AskGptCommand.class;
    }
}
