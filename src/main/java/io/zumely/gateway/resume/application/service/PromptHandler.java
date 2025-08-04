package io.zumely.gateway.resume.application.service;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.prompt.Prompt;

public interface PromptHandler {

    Command handle(Prompt prompt);
}
