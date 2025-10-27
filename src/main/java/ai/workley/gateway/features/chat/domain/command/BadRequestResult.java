package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.shared.app.command.results.Result;

public record BadRequestResult(String message) implements Result {
}
