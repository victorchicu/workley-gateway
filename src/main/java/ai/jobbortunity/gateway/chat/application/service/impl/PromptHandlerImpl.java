package ai.jobbortunity.gateway.chat.application.service.impl;

import ai.jobbortunity.gateway.chat.application.command.Command;
import ai.jobbortunity.gateway.chat.application.command.impl.CreateChatCommand;
import ai.jobbortunity.gateway.chat.application.service.PromptHandler;
import org.springframework.stereotype.Component;

@Component
public class PromptHandlerImpl implements PromptHandler {

    public Command handle(String prompt) {
        //todo: I need to figure out what the user wants in order to generate the appropriate command.
        return new CreateChatCommand(prompt);
    }
}
