package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.event.ApplicationEvent;

import java.security.Principal;

public record EmbeddingSavedApplicationEvent(Principal actor, Message<String> message) implements ApplicationEvent {
}
