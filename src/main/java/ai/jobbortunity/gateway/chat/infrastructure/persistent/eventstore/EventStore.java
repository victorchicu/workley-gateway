package ai.jobbortunity.gateway.chat.infrastructure.persistent.eventstore;

import ai.jobbortunity.gateway.chat.domain.event.DomainEvent;
import ai.jobbortunity.gateway.chat.infrastructure.persistent.eventstore.entity.EventModel;
import reactor.core.publisher.Mono;

public interface EventStore {

    <T extends DomainEvent> Mono<EventModel<T>> save(String actor, T object);
}
