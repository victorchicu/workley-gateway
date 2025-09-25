package ai.jobbortunity.gateway.chat.interfaces.rest.error;

import ai.jobbortunity.gateway.chat.application.command.CommandResult;

public record InternalError(String message) implements CommandResult {
}
