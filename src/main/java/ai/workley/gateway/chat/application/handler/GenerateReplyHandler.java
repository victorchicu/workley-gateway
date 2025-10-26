package ai.workley.gateway.chat.application.handler;

import ai.workley.gateway.chat.application.command.GenerateReply;
import ai.workley.gateway.chat.application.result.GenerateReplyResult;
import ai.workley.gateway.chat.domain.event.ReplyGenerated;
import ai.workley.gateway.chat.application.error.ApplicationError;
import ai.workley.gateway.chat.infrastructure.persistent.eventstore.EventStore;
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
            ReplyGenerated replyGenerated =
                    new ReplyGenerated(actor, command.chatId(), command.prompt().content());

            Mono<GenerateReplyResult> tx = transactionalOperator.transactional(
                    eventStore.save(actor, replyGenerated)
                            .thenReturn(GenerateReplyResult.empty()));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(replyGenerated));
        }).onErrorMap(error -> {
            log.error("Oops! Could not generate reply. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
