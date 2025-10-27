package ai.workley.gateway.features.chat.domain.command.results;

import ai.workley.gateway.features.shared.app.command.results.Result;

public record SaveEmbeddingResult() implements Result {
    private static final SaveEmbeddingResult EMPTY = new SaveEmbeddingResult();

    public static SaveEmbeddingResult empty() {
        return EMPTY;
    }
}
