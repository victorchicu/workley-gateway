package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.event.Event;

public class ErrorEvent extends Event {
    private final String error;

    public ErrorEvent(String aggregateId, String error) {
        super(aggregateId);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
