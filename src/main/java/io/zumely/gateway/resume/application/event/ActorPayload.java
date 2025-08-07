package io.zumely.gateway.resume.application.event;

import java.security.Principal;

public record ActorPayload<T extends ApplicationEvent>(Principal actor, T event) {
}
