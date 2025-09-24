package ai.jobbortunity.gateway.chat.application.service;

import ai.jobbortunity.gateway.chat.application.command.Command;

public interface PromptHandler {

    Command handle(String prompt);
}
