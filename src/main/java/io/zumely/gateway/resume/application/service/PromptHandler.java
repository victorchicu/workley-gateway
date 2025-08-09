package io.zumely.gateway.resume.application.service;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.data.Prompt;

public interface PromptHandler {

    Command handle(Prompt prompt);
}
