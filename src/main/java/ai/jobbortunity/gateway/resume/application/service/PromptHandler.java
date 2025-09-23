package ai.jobbortunity.gateway.resume.application.service;

import ai.jobbortunity.gateway.resume.application.command.Command;

public interface PromptHandler {

    Command handle(String prompt);
}
