package ai.workley.gateway.chat.infrastructure.eventbus;

import ai.workley.gateway.chat.domain.events.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ApplicationEventBus implements EventBus {
    private final ApplicationEventPublisher applicationEventPublisher;

    public ApplicationEventBus(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public <T extends DomainEvent> void publishEvent(T event) {
        applicationEventPublisher.publishEvent(event);
    }
}
