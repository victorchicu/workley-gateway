package ai.workley.gateway.features.chat.domain.command;

import ai.workley.gateway.features.shared.app.command.results.Output;

public record SaveEmbeddingOutput() implements Output {
    private static final SaveEmbeddingOutput EMPTY = new SaveEmbeddingOutput();

    public static SaveEmbeddingOutput empty() {
        return EMPTY;
    }
}
