package ai.jobbortunity.gateway.chat.infrastructure.eventstore;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.chat.infrastructure.data.EventObject;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface EventStore {

    <T extends ApplicationEvent> Mono<EventObject<T>> save(Principal actor, T object);
}
