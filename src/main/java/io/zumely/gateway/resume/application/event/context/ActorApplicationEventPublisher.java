package io.zumely.gateway.resume.application.event.context;

import io.zumely.gateway.resume.application.event.ActorEvent;
import io.zumely.gateway.resume.application.event.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class ActorApplicationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public ActorApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public <T extends ApplicationEvent> void publishEvent(Principal actor, T event) {
        publisher.publishEvent(new PayloadApplicationEvent<>(actor, new ActorEvent<>(actor, event)));
    }
}