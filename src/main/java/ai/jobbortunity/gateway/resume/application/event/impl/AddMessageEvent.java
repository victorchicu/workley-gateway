package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.application.command.Message;
import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;

import java.security.Principal;

public record AddMessageEvent(Principal actor, String chatId, Message<String> message) implements ApplicationEvent {
}
