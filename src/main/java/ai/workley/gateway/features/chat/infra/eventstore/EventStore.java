package ai.workley.gateway.features.chat.infra.eventstore;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;
import ai.workley.gateway.features.chat.infra.persistent.mongodb.document.EventDocument;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Mono<EventDocument<T>> save(String actor, T object);
}
