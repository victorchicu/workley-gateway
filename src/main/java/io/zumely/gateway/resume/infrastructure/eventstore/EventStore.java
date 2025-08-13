package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import io.zumely.gateway.resume.infrastructure.eventstore.data.EventObject;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface EventStore {

    <T extends ApplicationEvent> Mono<EventObject<T>> save(Principal actor, T object);
}
