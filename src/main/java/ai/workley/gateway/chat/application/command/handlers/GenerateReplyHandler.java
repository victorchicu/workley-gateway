package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.domain.command.GenerateReply;
import ai.workley.gateway.chat.domain.events.ReplyStarted;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.payloads.GenerateReplyPayload;
import ai.workley.gateway.chat.infrastructure.eventstore.EventStore;
import ai.workley.gateway.chat.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class GenerateReplyHandler implements CommandHandler<GenerateReply, GenerateReplyPayload> {
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
    public Mono<GenerateReplyPayload> handle(String actor, GenerateReply command) {
        return Mono.defer(() -> {
            ReplyStarted replyStarted =
                    new ReplyStarted(actor, command.chatId(), command.message());

            Mono<GenerateReplyPayload> tx = transactionalOperator.transactional(
                    eventStore.append(actor, replyStarted, null)
                            .thenReturn(GenerateReplyPayload.ack()));

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(replyStarted));
        }).onErrorMap(error -> {
            log.error("Oops! Could not generate message. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
