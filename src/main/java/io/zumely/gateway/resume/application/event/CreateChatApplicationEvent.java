package io.zumely.gateway.resume.application.event;

public record CreateChatApplicationEvent(String chatId, String prompt) implements ApplicationEvent {
}