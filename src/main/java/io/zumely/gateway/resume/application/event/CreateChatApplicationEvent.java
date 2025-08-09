package io.zumely.gateway.resume.application.event;

import io.zumely.gateway.resume.application.command.data.Prompt;

public record CreateChatApplicationEvent(String chatId, Prompt prompt) implements ApplicationEvent {
}
