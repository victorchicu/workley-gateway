package ai.workley.gateway.features.chat.app.command.handler;

import ai.workley.gateway.features.chat.domain.command.GenerateReplyInput;
import ai.workley.gateway.features.chat.domain.command.GenerateReplyOutput;
import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.chat.infra.eventstore.EventStore;
import ai.workley.gateway.features.chat.domain.event.ReplyGenerated;
import ai.workley.gateway.features.shared.app.command.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class GenerateReplyHandler implements CommandHandler<GenerateReplyInput, GenerateReplyOutput> {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyHandler.class);

    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GenerateReplyHandler(
            EventStore eventStore,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<GenerateReplyInput> supported() {
        return GenerateReplyInput.class;
    }

    @Override
    public Mono<GenerateReplyOutput> handle(String actor, GenerateReplyInput command) {
        return Mono.defer(() -> {
            ReplyGenerated replyGenerated =
                    new ReplyGenerated(actor, command.chatId(), command.prompt());

            Mono<GenerateReplyOutput> tx = transactionalOperator.transactional(
                    eventStore.save(actor, replyGenerated)
                            .thenReturn(GenerateReplyOutput.empty()));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(replyGenerated));
        }).onErrorMap(error -> {
            log.error("Oops! Could not generate reply. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
