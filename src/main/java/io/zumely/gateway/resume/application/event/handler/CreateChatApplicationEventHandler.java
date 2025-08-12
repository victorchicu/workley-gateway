package io.zumely.gateway.resume.application.event.handler;

import io.zumely.gateway.resume.application.event.data.CreateChatApplicationEvent;
import io.zumely.gateway.resume.application.exception.ApplicationException;
import io.zumely.gateway.resume.infrastructure.eventstore.ChatStore;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreateChatApplicationEventHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateChatApplicationEventHandler.class);

    private final ChatStore chatStore;

    public CreateChatApplicationEventHandler(ChatStore chatStore) {
        this.chatStore = chatStore;
    }

    @EventListener
    public Mono<StoreEvent<CreateChatApplicationEvent>> handle(CreateChatApplicationEvent source) {
        String actor = source.actor().getName();
        return chatStore.save(actor, source)
                .doOnSuccess((StoreEvent<CreateChatApplicationEvent> event) ->
                        log.info("Saved {} event for actor {}",
                                source.getClass().getSimpleName(), actor))
                .doOnError(error ->
                        log.error("Oops! Something went wrong while saving event {} for actor {}",
                                source.getClass().getSimpleName(), actor, error))
                .onErrorResume(error -> Mono.error(new ApplicationException("Oops! Chat not saved.")));
    }
}
