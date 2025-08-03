package io.zumely.gateway.resume.infrastructure.eventstore;

import io.zumely.gateway.resume.application.event.Event;
import io.zumely.gateway.resume.infrastructure.eventstore.objects.StoredEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventStore {

    Mono<Void> save(Event event);

    Flux<StoredEvent> findEventsByAggregateId(String aggregateId);
}
