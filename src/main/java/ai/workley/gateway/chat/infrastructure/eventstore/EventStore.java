package ai.workley.gateway.chat.infrastructure.eventstore;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import ai.workley.gateway.chat.infrastructure.persistent.mongodb.documents.EventDocument;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Mono<EventDocument<T>> save(String actor, T object);
}
