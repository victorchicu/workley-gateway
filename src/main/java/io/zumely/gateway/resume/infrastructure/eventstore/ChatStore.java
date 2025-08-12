package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.data.StoreEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatStore {

    <T extends ApplicationEvent> Mono<StoreEvent<T>> save(String actor, T object);
}
