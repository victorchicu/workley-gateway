package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.event.Event;

public class ErrorEvent extends Event {
    private final String cause;

    public ErrorEvent(String aggregateId, String cause) {
        super(aggregateId);
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }
}
