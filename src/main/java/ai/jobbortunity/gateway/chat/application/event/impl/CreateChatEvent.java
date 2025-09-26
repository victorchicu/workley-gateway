package ai.jobbortunity.gateway.chat.application.event.impl;

import ai.jobbortunity.gateway.chat.application.event.ApplicationEvent;

public record CreateChatEvent(String actor, String chatId, String prompt) implements ApplicationEvent {
}
