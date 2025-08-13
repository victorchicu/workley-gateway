package io.zumely.gateway.resume.application.event.data;

import io.zumely.gateway.resume.application.command.data.Message;

import java.security.Principal;

public record MessageAddedApplicationEvent(Principal actor, String chatId, Message<String> message) implements ApplicationEvent {
}