package ai.workley.gateway.chat.domain.payloads;

public record SaveEmbeddingPayload() implements Payload {
    private static final SaveEmbeddingPayload EMPTY = new SaveEmbeddingPayload();

    public static SaveEmbeddingPayload empty() {
        return EMPTY;
    }
}
