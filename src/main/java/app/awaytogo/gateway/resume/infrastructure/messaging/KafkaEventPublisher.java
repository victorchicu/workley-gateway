package app.awaytogo.gateway.resume.infrastructure.messaging;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisher {
    public Publisher<?> publish(DomainEvent domainEvent) {
        return null;
    }
}
