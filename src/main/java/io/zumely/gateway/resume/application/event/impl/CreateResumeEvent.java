package io.zumely.gateway.resume.application.event.impl;

import io.zumely.gateway.resume.application.event.Event;

public class CreateResumeEvent extends Event {
    private final String prompt;

    public CreateResumeEvent(String prompt, String aggregateId) {
        super(aggregateId);
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
