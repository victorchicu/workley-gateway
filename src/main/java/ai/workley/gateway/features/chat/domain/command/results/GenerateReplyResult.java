package ai.workley.gateway.features.chat.domain.command.results;

import ai.workley.gateway.features.shared.app.command.results.Result;

public record GenerateReplyResult() implements Result {
    private static final GenerateReplyResult EMPTY = new GenerateReplyResult();

    public static GenerateReplyResult empty() {
        return EMPTY;
    }
}
