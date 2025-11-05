package ai.workley.gateway.chat.domain;

public record Embedding(String id, String model, String actor, Integer dimension, float[] embedding) {

    public static Embedding create(String model, String actor, Integer dimension, float[] embedding) {
        return new Embedding(null, model, actor, dimension, embedding);
    }
}
