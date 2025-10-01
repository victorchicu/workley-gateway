package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.chat.application.service.Intent;

public record GenerateReplyEvent(String actor, String chatId, Intent intent, String prompt) implements ApplicationEvent {

}
