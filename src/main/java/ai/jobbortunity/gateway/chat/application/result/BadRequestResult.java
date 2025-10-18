package ai.jobbortunity.gateway.chat.application.result;

public record BadRequestResult(String message) implements CommandResult, QueryResult {
}
