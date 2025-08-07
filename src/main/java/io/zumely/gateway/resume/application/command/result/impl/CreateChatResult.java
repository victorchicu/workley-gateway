package io.zumely.gateway.resume.application.command.result.impl;

import io.zumely.gateway.resume.application.command.result.Result;
import io.zumely.gateway.resume.application.event.ApplicationEvent;

import java.util.Objects;

public record CreateChatResult(String chatId, String firstReply) implements Result {
    public CreateChatResult(String chatId, String firstReply) {
        this.chatId = Objects.requireNonNull(chatId, "chatId must not be null");
        this.firstReply = Objects.requireNonNull(firstReply, "firstReply must not be null");
    }

    public static <T extends ApplicationEvent> CreateChatResult firstReply(String chatId) {
        return new CreateChatResult(
                chatId,
                "Great! I'm working on your resume now..."
        );
    }
}