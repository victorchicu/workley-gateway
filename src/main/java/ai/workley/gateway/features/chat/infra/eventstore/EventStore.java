package ai.workley.gateway.features.chat.infra.eventstore;

import ai.workley.gateway.features.chat.domain.event.DomainEvent;
import ai.workley.gateway.features.chat.infra.readmodel.EventModel;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Mono<EventModel<T>> save(String actor, T object);
}
