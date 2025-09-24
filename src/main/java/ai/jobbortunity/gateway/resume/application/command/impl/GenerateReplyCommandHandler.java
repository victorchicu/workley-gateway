package ai.jobbortunity.gateway.resume.application.command.impl;

import ai.jobbortunity.gateway.resume.application.command.CommandHandler;
import ai.jobbortunity.gateway.resume.application.event.impl.GenerateReplyEvent;
import ai.jobbortunity.gateway.resume.application.exception.ApplicationException;
import ai.jobbortunity.gateway.resume.infrastructure.data.EventObject;
import ai.jobbortunity.gateway.resume.infrastructure.eventstore.EventStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class GenerateReplyCommandHandler implements CommandHandler<GenerateReplyCommand, GenerateReplyCommandResult> {
    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GenerateReplyCommandHandler(
            EventStore eventStore,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Mono<GenerateReplyCommandResult> handle(Principal actor, GenerateReplyCommand command) {
        GenerateReplyEvent generateReplyEvent =
                new GenerateReplyEvent(actor, command.chatId(), command.prompt());
        return Mono.defer(() ->
                        eventStore.save(actor, generateReplyEvent)
                                .map((EventObject<GenerateReplyEvent> eventObject) -> {
                                    applicationEventPublisher.publishEvent(eventObject.getEventData());
                                    return GenerateReplyCommandResult.empty();
                                })
                )
                .as(transactionalOperator::transactional)
                .onErrorMap(error -> new ApplicationException("Oops! Could not generate reply."));
    }

    @Override
    public Class<GenerateReplyCommand> supported() {
        return GenerateReplyCommand.class;
    }
}
