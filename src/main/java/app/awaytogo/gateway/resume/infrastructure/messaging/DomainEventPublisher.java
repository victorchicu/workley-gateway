package app.awaytogo.gateway.resume.infrastructure.messaging;

import app.awaytogo.gateway.resume.domain.DomainEvent;
import reactor.core.publisher.Mono;

public interface DomainEventPublisher {

    Mono<Void> publish(DomainEvent event);

    // Mono<Void> publish(List<? extends DomainEvent> events);
}
