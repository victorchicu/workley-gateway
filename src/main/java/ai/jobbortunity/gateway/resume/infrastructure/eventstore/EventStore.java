package ai.jobbortunity.gateway.resume.infrastructure.eventstore;

import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.resume.infrastructure.data.EventObject;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface EventStore {

    <T extends ApplicationEvent> Mono<EventObject<T>> save(Principal actor, T object);
}
