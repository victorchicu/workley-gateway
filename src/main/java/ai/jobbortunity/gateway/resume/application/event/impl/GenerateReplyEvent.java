package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.application.command.Message;
import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;

import java.security.Principal;

public record GenerateReplyEvent(Principal actor, String chatId, Message<String> prompt) implements ApplicationEvent {

}
