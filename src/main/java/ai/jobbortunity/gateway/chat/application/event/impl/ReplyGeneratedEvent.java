package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

public record ReplyGeneratedEvent(String actor, String chatId, String reply) implements ApplicationEvent {
}
