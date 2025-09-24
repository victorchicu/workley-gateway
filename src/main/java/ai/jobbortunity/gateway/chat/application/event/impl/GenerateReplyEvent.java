package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

import java.security.Principal;

public record GenerateReplyEvent(Principal actor, String chatId, Message<String> prompt) implements ApplicationEvent {

}
