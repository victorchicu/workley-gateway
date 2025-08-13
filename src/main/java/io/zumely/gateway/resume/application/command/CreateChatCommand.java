package io.zumely.gateway.resume.application.command;

public record CreateChatCommand(String prompt) implements Command {
}