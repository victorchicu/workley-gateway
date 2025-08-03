package io.zumely.gateway.resume.application.prompt;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.prompt.command.Prompt;

public interface PromptHandler {

    Command handle(Prompt prompt);
}
