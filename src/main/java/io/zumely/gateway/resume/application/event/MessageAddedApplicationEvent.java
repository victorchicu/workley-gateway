package io.zumely.gateway.resume.application.event;

import io.zumely.gateway.resume.application.command.Message;

import java.security.Principal;

public record MessageAddedApplicationEvent(Principal actor, String chatId, Message<String> message) implements ApplicationEvent {
}