package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.event.Event;

import java.security.Principal;

public class ErrorEvent extends Event {
    private final String message;

    public ErrorEvent(Principal principal, String chatId, String message) {
        super(principal, chatId);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}