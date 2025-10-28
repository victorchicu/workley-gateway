package ai.workley.gateway.features.chat.app.command.handler;

import ai.workley.gateway.features.chat.app.error.ApplicationError;
import ai.workley.gateway.features.chat.domain.command.SaveEmbeddingInput;
import ai.workley.gateway.features.chat.domain.command.SaveEmbeddingOutput;
import ai.workley.gateway.features.chat.domain.event.EmbeddingSaved;
import ai.workley.gateway.features.chat.infra.eventstore.EventStore;
import ai.workley.gateway.features.shared.app.command.handler.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class SaveEmbeddingHandler implements CommandHandler<SaveEmbeddingInput, SaveEmbeddingOutput> {
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
    public Class<SaveEmbeddingInput> supported() {
        return SaveEmbeddingInput.class;
    }

    @Override
    public Mono<SaveEmbeddingOutput> handle(String actor, SaveEmbeddingInput command) {
        return Mono.defer(() -> {
            EmbeddingSaved embeddingSaved =
                    new EmbeddingSaved(actor, command.text(), Collections.emptyMap());

            Mono<SaveEmbeddingOutput> tx = transactionalOperator.transactional(
                    eventStore.save(actor, embeddingSaved)
                            .thenReturn(SaveEmbeddingOutput.empty())
            );

            return tx.doOnSuccess(__ -> applicationEventPublisher.publishEvent(embeddingSaved));
        }).onErrorMap(error -> {
            log.error("Oops! Could not save embedding. text={}", command.text(), error);
            return (error instanceof ApplicationError) ? error
                    : new ApplicationError("Oops! Something went wrong, please try again.", error);
        });
    }
}
