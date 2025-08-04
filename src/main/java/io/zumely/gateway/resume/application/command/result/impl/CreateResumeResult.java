package io.zumely.gateway.resume.application.command.result.impl;

import io.zumely.gateway.resume.application.command.result.Result;
import io.zumely.gateway.resume.application.command.result.CreationStep;
import io.zumely.gateway.resume.application.event.Event;

import java.util.Objects;

public record CreateResumeResult(String aggregateId, CreationStep creationStep) implements Result {
    public CreateResumeResult(String aggregateId, CreationStep creationStep) {
        this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        this.creationStep = Objects.requireNonNull(creationStep, "creationStep must not be null");
    }

    public static <T extends Event> CreateResumeResult asDraft(T event) {
        return new CreateResumeResult(event.getAggregateId(), CreationStep.DRAFT);
    }
}
