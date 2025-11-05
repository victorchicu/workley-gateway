package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.command.SaveEmbedding;
import ai.workley.gateway.chat.domain.payloads.SaveEmbeddingPayload;
import ai.workley.gateway.chat.domain.events.EmbeddingSaved;
import ai.workley.gateway.chat.infrastructure.eventstore.EventStore;
import ai.workley.gateway.chat.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class SaveEmbeddingHandler implements CommandHandler<SaveEmbedding, SaveEmbeddingPayload> {
    private static final Logger log = LoggerFactory.getLogger(SaveEmbeddingHandler.class);

    private final EventStore eventStore;
    private final TransactionalOperator transactionalOperator;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SaveEmbeddingHandler(
            EventStore eventStore,
            TransactionalOperator transactionalOperator,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.eventStore = eventStore;
        this.transactionalOperator = transactionalOperator;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<SaveEmbedding> supported() {
        return SaveEmbedding.class;
    }

    @Override
    public Mono<SaveEmbeddingPayload> handle(String actor, SaveEmbedding command) {
        return Mono.defer(() -> {
            EmbeddingSaved embeddingSaved =
                    new EmbeddingSaved(actor, command.text(), Collections.emptyMap());

            Mono<SaveEmbeddingPayload> tx = transactionalOperator.transactional(
                    eventStore.save(actor, embeddingSaved)
                            .thenReturn(SaveEmbeddingPayload.empty())
            );

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(embeddingSaved));
        }).onErrorMap(error -> {
            log.error("Oops! Could not save embedding. text={}", command.text(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
