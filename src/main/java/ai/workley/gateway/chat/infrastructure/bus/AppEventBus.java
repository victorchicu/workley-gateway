package ai.workley.gateway.chat.infrastructure.bus;

import ai.workley.gateway.chat.application.ports.outbound.EventBus;
import ai.workley.gateway.chat.domain.events.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AppEventBus implements EventBus {
    private final ApplicationEventPublisher applicationEventPublisher;

    public AppEventBus(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public <T extends DomainEvent> void publishEvent(T event) {
        applicationEventPublisher.publishEvent(event);
    }
}
