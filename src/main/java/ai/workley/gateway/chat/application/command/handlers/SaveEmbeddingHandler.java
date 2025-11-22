package ai.workley.gateway.chat.application.command.handlers;

import ai.workley.gateway.chat.application.eventstore.EventService;
import ai.workley.gateway.chat.application.exceptions.ApplicationError;
import ai.workley.gateway.chat.domain.aggregations.AggregateTypes;
import ai.workley.gateway.chat.domain.command.SaveEmbedding;
import ai.workley.gateway.chat.domain.payloads.SaveEmbeddingPayload;
import ai.workley.gateway.chat.domain.events.EmbeddingSaved;
import ai.workley.gateway.chat.application.ports.outbound.bus.EventBus;
import ai.workley.gateway.chat.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

@Component
public class SaveEmbeddingHandler implements CommandHandler<SaveEmbedding, SaveEmbeddingPayload> {
    private static final Logger log = LoggerFactory.getLogger(SaveEmbeddingHandler.class);

    private final EventBus eventBus;
    private final EventService eventService;
    private final TransactionalOperator transactionalOperator;

    public SaveEmbeddingHandler(
            EventBus eventBus,
            EventService eventService,
            TransactionalOperator transactionalOperator
    ) {
        this.eventBus = eventBus;
        this.eventService = eventService;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Class<SaveEmbedding> supported() {
        return SaveEmbedding.class;
    }

    @Override
    public Mono<SaveEmbeddingPayload> handle(String actor, SaveEmbedding command) {
        return handle(actor, command, null);
    }

    @Override
    public Mono<SaveEmbeddingPayload> handle(String actor, SaveEmbedding command, String idempotencyKey) {
        return Mono.defer(() -> {
            EmbeddingSaved embeddingSaved =
                    new EmbeddingSaved(actor, command.text(), Collections.emptyMap());

            Mono<SaveEmbeddingPayload> tx = transactionalOperator.transactional(
                    eventService.append(embeddingSaved, AggregateTypes.EMBEDDING, UUID.randomUUID().toString(), null)
                            .thenReturn(SaveEmbeddingPayload.empty())
            );

            return tx.doOnSuccess(__ -> eventBus.publishEvent(embeddingSaved));
        }).onErrorMap(error -> {
            log.error("Oops! Could not save embedding. text={}", command.text(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
