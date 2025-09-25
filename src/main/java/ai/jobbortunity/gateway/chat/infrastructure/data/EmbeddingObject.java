package ai.jobbortunity.gateway.chat.infrastructure.data;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "embeddings")
public class EmbeddingObject {
    @Id
    private String id;
    private String model;
    private String actor;
    private String type;
    private String reference;
    private Integer dimension;
    private float[] embedding;
    @CreatedDate
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public EmbeddingObject setId(String id) {
        this.id = id;
        return this;
    }

    public String getModel() {
        return model;
    }

    public EmbeddingObject setModel(String model) {
        this.model = model;
        return this;
    }

    public String getActor() {
        return actor;
    }

    public EmbeddingObject setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public String getType() {
        return type;
    }

    public EmbeddingObject setType(String type) {
        this.type = type;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public EmbeddingObject setReference(String reference) {
        this.reference = reference;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public EmbeddingObject setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public EmbeddingObject setEmbedding(float[] embedding) {
        this.embedding = embedding;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EmbeddingObject setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
