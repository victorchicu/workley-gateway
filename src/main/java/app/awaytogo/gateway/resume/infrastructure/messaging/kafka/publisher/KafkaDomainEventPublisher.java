package app.awaytogo.gateway.resume.infrastructure.messaging.kafka.publisher;

import app.awaytogo.gateway.resume.domain.DomainEvent;
import app.awaytogo.gateway.resume.infrastructure.messaging.DomainEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    @Override
    public Mono<Void> publish(DomainEvent event) {
        return null;
    }
}
