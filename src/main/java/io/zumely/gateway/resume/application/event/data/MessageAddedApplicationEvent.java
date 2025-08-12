package io.zumely.gateway.resume.application.event.data;

import io.zumely.gateway.resume.application.command.data.Message;

public record MessageAddedApplicationEvent(String actor, String chatId, Message<String> message) implements ApplicationEvent {
}
