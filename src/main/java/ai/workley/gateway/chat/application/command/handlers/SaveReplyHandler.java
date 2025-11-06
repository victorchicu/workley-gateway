package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.command.CommandHandler;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.command.SaveReply;
import ai.workley.gateway.chat.domain.events.ReplySaved;
import ai.workley.gateway.chat.domain.payloads.ReplySavedPayload;
import ai.workley.gateway.chat.infrastructure.eventstore.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class SaveReplyHandler implements CommandHandler<SaveReply, ReplySavedPayload> {
    private static final Logger log = LoggerFactory.getLogger(GenerateReplyHandler.class);

    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;

    public SaveReplyHandler(EventStore eventStore, TransactionalOperator transactionalOperator) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Class<SaveReply> supported() {
        return SaveReply.class;
    }

    @Override
    public Mono<ReplySavedPayload> handle(String actor, SaveReply command) {
        return Mono.defer(() -> {
            ReplySaved replySaved =
                    new ReplySaved(actor, command.chatId(), command.reply());

            return transactionalOperator.transactional(
                    eventStore.append(actor, replySaved, null)
                            .thenReturn(ReplySavedPayload.ack()));
        }).onErrorMap(error -> {
            log.error("Oops! Could not save reply. chatId={}", command.chatId(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
