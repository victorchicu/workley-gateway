package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

public record SaveEmbeddingEvent(String actor, String type, String reference, String text) implements ApplicationEvent {
}
