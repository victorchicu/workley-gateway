package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.event.Event;

import java.security.Principal;

public class CreateChatEvent extends Event {
    private final String prompt;

    public CreateChatEvent(Principal principal, String chatId, String prompt) {
        super(principal, chatId);
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}