package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.shared.app.command.results.Output;

public record GenerateReplyOutput() implements Output {
    private static final GenerateReplyOutput EMPTY = new GenerateReplyOutput();

    public static GenerateReplyOutput empty() {
        return EMPTY;
    }
}
