package io.zumely.gateway.resume.application.prompt.impl;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.prompt.command.Prompt;
import io.zumely.gateway.resume.application.command.impl.CreateResumeCommand;
import io.zumely.gateway.resume.application.prompt.PromptHandler;
import org.springframework.stereotype.Component;

@Component
public class PromptHandlerImpl implements PromptHandler {

    public Command handle(Prompt prompt) {
        //todo: I need to figure out what the user wants in order to generate the appropriate command.

        return new CreateResumeCommand(prompt.text());
    }
}
