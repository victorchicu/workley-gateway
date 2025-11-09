package ai.workley.gateway.chat.infrastructure.embedding;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "embeddings")
public class EmbeddingDocument {
    @Id
    private String id;
    private String model;
    private String actor;
    private Integer dimension;
    private float[] embedding;
    @CreatedDate
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public EmbeddingDocument setId(String id) {
        this.id = id;
        return this;
    }

    public String getModel() {
        return model;
    }

    public EmbeddingDocument setModel(String model) {
        this.model = model;
        return this;
    }

    public String getActor() {
        return actor;
    }

    public EmbeddingDocument setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public EmbeddingDocument setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public EmbeddingDocument setEmbedding(float[] embedding) {
        this.embedding = embedding;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EmbeddingDocument setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
