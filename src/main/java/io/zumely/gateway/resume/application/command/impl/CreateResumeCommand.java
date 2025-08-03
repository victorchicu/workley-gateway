package io.zumely.gateway.resume.application.command.impl;

import io.zumely.gateway.resume.application.command.Command;

public record CreateResumeCommand(String prompt) implements Command {
}
