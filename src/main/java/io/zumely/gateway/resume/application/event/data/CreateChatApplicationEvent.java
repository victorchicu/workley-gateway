package io.zumely.gateway.resume.application.event.data;

import io.zumely.gateway.resume.application.command.data.Prompt;

public record CreateChatApplicationEvent(String actor, String chatId, Prompt prompt) implements ApplicationEvent {
}