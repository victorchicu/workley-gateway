package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.command.Message;
import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.chat.application.service.Intent;

import java.security.Principal;

public record GenerateReplyEvent(String actor, String chatId, Intent intent, String prompt) implements ApplicationEvent {

}
