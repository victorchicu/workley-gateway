package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

import java.security.Principal;

public record AddMessageEvent(String actor, String chatId, Message<String> message) implements ApplicationEvent {
}
