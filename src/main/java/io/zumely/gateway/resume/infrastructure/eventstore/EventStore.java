package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.application.event.Event;
import io.zumely.gateway.resume.infrastructure.eventstore.objects.StoredEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface EventStore {

    Mono<StoredEvent> save(Event event);

    Flux<StoredEvent> findEvents(Principal principal, String chatId);
}