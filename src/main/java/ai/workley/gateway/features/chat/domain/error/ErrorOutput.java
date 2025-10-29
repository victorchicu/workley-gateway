package ai.workley.gateway.features.chat.domain.error;

import ai.workley.gateway.features.shared.app.command.results.Output;

public record ErrorOutput(String message) implements Output {
}
