package io.zumely.gateway.resume.application.event.handler;

import io.zumely.gateway.resume.application.event.impl.ErrorEvent;
import io.zumely.gateway.resume.application.event.impl.CreateResumeEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CreateResumeEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateResumeEventHandler.class);

    private final EventStore eventStore;

    public CreateResumeEventHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @EventListener
    @Async
    public CompletableFuture<Void> handle(CreateResumeEvent source) {
        try {
            return eventStore.save(source)
                    .doOnSuccess(v ->
                            log.info("Event saved: {}",
                                    source.getClass().getSimpleName()))
                    .doOnError(error -> log.error("Failed to save event", error))
                    .toFuture();
        } catch (Exception e) {
            log.error("Error handling event", e);
            return eventStore.save(
                            new ErrorEvent(
                                    source.getAggregateId(),
                                    String.join("\n", ExceptionUtils.getStackFrames(e))))
                    .toFuture();
        }
    }
}
