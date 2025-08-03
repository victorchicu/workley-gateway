package io.zumely.gateway.resume.application.command.result.impl;

import io.zumely.gateway.resume.application.command.result.Result;

public record CreateResumeResult(String aggregateId) implements Result {
}
