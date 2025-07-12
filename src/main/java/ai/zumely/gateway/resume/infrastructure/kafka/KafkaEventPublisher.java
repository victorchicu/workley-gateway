package ai.zumely.gateway.resume.infrastructure.kafka;

import ai.zumely.gateway.resume.domain.event.DomainEvent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KafkaEventPublisher {

    public Publisher<?> publish(DomainEvent domainEvent) {

        return Mono.empty();
    }
}
