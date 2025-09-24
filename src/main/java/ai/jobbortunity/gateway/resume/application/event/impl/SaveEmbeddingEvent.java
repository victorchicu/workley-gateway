package ai.jobbortunity.gateway.resume.application.event.impl;

import ai.jobbortunity.gateway.resume.application.command.Message;
import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;

public record SaveEmbeddingEvent(String actor, String chatId, Message<String> message) implements ApplicationEvent {
}