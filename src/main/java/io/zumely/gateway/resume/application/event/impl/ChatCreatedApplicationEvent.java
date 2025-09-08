package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.command.Message;
import io.zumely.gateway.resume.application.event.ApplicationEvent;

import java.security.Principal;

public record ChatCreatedApplicationEvent(Principal actor, String chatId, Message<String> message) implements ApplicationEvent {
}
