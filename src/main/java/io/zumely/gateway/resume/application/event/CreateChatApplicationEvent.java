package io.zumely.gateway.resume.application.event;

public record CreateChatApplicationEvent(String prompt, String chatId) implements ApplicationEvent {
}
