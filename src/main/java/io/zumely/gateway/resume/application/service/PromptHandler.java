package io.zumely.gateway.resume.application.service;

import io.zumely.gateway.resume.application.command.Command;

public interface PromptHandler {

    Command handle(String prompt);
}
