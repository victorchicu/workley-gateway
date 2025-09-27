package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

import java.util.Map;

public record SaveEmbeddingEvent(String actor, String text, Map<String, Object> metadata) implements ApplicationEvent {
}
