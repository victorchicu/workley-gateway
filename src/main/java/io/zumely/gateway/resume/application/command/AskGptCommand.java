package io.zumely.gateway.resume.application.command;

public record AskGptCommand(String prompt) implements Command {
}
