package io.zumely.gateway.resume.application.service.impl;

import io.zumely.gateway.resume.application.command.Command;
import io.zumely.gateway.resume.application.command.prompt.Prompt;
import io.zumely.gateway.resume.application.command.impl.CreateChatCommand;
import io.zumely.gateway.resume.application.service.PromptHandler;
import org.springframework.stereotype.Component;

@Component
public class PromptHandlerImpl implements PromptHandler {

    public Command handle(Prompt prompt) {
        //todo: I need to figure out what the user wants in order to generate the appropriate command.

        return new CreateChatCommand(prompt.text());
    }
}
