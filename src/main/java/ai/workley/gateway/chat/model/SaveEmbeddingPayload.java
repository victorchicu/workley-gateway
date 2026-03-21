package ai.workley.gateway.chat.model;

public record SaveEmbeddingPayload() implements Payload {
    private static final SaveEmbeddingPayload EMPTY = new SaveEmbeddingPayload();

    public static SaveEmbeddingPayload empty() {
        return EMPTY;
    }
}
