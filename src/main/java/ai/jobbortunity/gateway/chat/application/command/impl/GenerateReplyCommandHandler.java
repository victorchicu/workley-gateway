package ai.jobbortunity.gateway.chat.application.command.impl;

import ai.jobbortunity.gateway.chat.application.command.CommandHandler;
import ai.jobbortunity.gateway.chat.application.event.impl.GenerateReplyEvent;
import ai.jobbortunity.gateway.chat.application.exception.ApplicationException;
import ai.jobbortunity.gateway.chat.infrastructure.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class GenerateReplyCommandHandler implements CommandHandler<GenerateReplyCommand, GenerateReplyCommandResult> {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyCommandHandler.class);

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
    public Class<GenerateReplyCommand> supported() {
        return GenerateReplyCommand.class;
    }

    @Override
    public Mono<GenerateReplyCommandResult> handle(String actor, GenerateReplyCommand command) {
        return Mono.defer(() -> {
            GenerateReplyEvent generateReplyEvent =
                    new GenerateReplyEvent(actor, command.chatId(), command.intent(), command.prompt().content());

            Mono<GenerateReplyCommandResult> tx = transactionalOperator.transactional(
                    eventStore.save(actor, generateReplyEvent)
                            .thenReturn(GenerateReplyCommandResult.empty()));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(generateReplyEvent));
        }).onErrorMap(error -> {
            log.error("Oops! Could not generate reply. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationException) ? error
                    : new ApplicationException("Oops! Could not generate reply.", error);
        });
    }
}
