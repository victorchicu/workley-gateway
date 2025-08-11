package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.application.event.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoredEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface EventStore {

    <T extends ApplicationEvent> Mono<StoredEvent<T>> saveEvent(Principal actor, T applicationEvent);

    <T extends ApplicationEvent> Flux<StoredEvent<T>> getChatHistory(Principal actor, String chatId);
}
