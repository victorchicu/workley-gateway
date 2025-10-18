package ai.jobbortunity.gateway.chat.application.result;

public record SaveEmbeddingResult() implements Result {
    private static final SaveEmbeddingResult EMPTY = new SaveEmbeddingResult();

    public static SaveEmbeddingResult empty() {
        return EMPTY;
    }
}
