package io.zumely.gateway.resume.application.command;

public record InternalErrorCommandResult(String message) implements CommandResult {
}
