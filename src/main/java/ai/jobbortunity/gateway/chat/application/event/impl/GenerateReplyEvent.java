package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;
import ai.jobbortunity.gateway.chat.application.service.ClassificationResult;

public record GenerateReplyEvent(String actor, String chatId, ClassificationResult classificationResult, String prompt) implements ApplicationEvent {

}
