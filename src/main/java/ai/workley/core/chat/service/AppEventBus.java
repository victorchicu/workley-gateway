package ai.workley.core.chat.service;

import ai.workley.core.chat.model.DomainEvent;
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
