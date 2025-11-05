package ai.workley.gateway.chat.domain.payloads;

import ai.workley.gateway.chat.domain.Payload;

public record SaveEmbeddingPayload() implements Payload {
    private static final SaveEmbeddingPayload EMPTY = new SaveEmbeddingPayload();

    public static SaveEmbeddingPayload empty() {
        return EMPTY;
    }
}
