package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatStore {

    Mono<Boolean> exists(String actor, String chatId);

    <T extends ApplicationEvent> Mono<StoreObject<T>> save(String actor, T object);

    <T extends ApplicationEvent> Flux<StoreObject<T>> findHistory(String actor, String chatId);
}