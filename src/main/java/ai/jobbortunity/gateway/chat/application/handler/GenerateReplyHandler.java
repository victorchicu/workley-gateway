package ai.jobbortunity.gateway.chat.application.handler;

import ai.jobbortunity.gateway.chat.application.command.GenerateReply;
import ai.jobbortunity.gateway.chat.application.result.GenerateReplyResult;
import ai.jobbortunity.gateway.chat.domain.event.ReplyInitiated;
import ai.jobbortunity.gateway.chat.application.error.ApplicationError;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class GenerateReplyHandler implements CommandHandler<GenerateReply, GenerateReplyResult> {
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
    public Class<GenerateReply> supported() {
        return GenerateReply.class;
    }

    @Override
    public Mono<GenerateReplyResult> handle(String actor, GenerateReply command) {
        return Mono.defer(() -> {
            ReplyInitiated replyInitiated =
                    new ReplyInitiated(actor, command.chatId(), command.classificationResult(), command.prompt().content());

            Mono<GenerateReplyResult> tx = transactionalOperator.transactional(
                    eventStore.save(actor, replyInitiated)
                            .thenReturn(GenerateReplyResult.empty()));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(replyInitiated));
        }).onErrorMap(error -> {
            log.error("Oops! Could not generate reply. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Could not generate reply.", error);
        });
    }
}
