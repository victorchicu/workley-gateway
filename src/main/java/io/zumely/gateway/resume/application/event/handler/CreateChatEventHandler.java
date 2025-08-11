package io.zumely.gateway.resume.application.event.handler;

import io.zumely.gateway.resume.application.event.ActorPayload;
import io.zumely.gateway.resume.application.event.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.eventstore.EventStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoredEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Component
public class CreateChatEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateChatEventHandler.class);

    private final EventStore eventStore;

    public CreateChatEventHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @EventListener
    public Mono<StoredEvent<CreateChatApplicationEvent>> handle(PayloadApplicationEvent<ActorPayload<CreateChatApplicationEvent>> source) {
        Principal actor = source.getPayload().actor();
        return eventStore.saveEvent(actor, source.getPayload().event())
                .doOnSuccess(event ->
                        log.info("Saved {} event for actor {}",
                                source.getClass().getSimpleName(), actor.getName()))
                .onErrorResume(error -> {
                    return Mono.error(
                            new ApplicationException(
                                    String.join("\n", ExceptionUtils.getStackFrames(error))));
                })
                .doOnError(error ->
                        log.error("Oops! Something went wrong while saving event {} for actor {}",
                                source.getClass().getSimpleName(), actor.getName(),
                                error));
    }
}
