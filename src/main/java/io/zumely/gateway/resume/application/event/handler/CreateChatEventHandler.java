package io.zumely.gateway.resume.application.event.handler;

import io.zumely.gateway.resume.application.event.impl.ErrorEvent;
import io.zumely.gateway.resume.application.event.impl.CreateChatEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.objects.StoredEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreateChatEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateChatEventHandler.class);

    private final EventStore eventStore;

    public CreateChatEventHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @EventListener
    public Mono<StoredEvent> handle(CreateChatEvent source) {
        return eventStore.save(source)
                .doOnSuccess(event ->
                        log.info("Saved {} event for aggregate {}",
                                source.getClass().getSimpleName(), source.getChatId()))
                .onErrorResume(error -> {
                    String str = "Failed to save event %s for aggregate %s"
                            .formatted(source.getClass().getSimpleName(),
                                    source.getChatId());

                    log.error(str, error);
                    return eventStore.save(
                            new ErrorEvent(source.getPrincipal(), source.getChatId(),
                                    String.join("\n", ExceptionUtils.getStackFrames(error))));
                })
                .doOnError(error ->
                        log.error("Something went wrong while saving event for aggregate: {}",
                                source.getChatId(), error));
    }
}