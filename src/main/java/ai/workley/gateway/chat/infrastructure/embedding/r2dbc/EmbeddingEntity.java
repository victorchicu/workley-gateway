package ai.workley.gateway.chat.infrastructure.embedding.r2dbc;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("embeddings")
public class EmbeddingEntity {
    @Id
    private Long id;
    private String model;
    private String actor;
    private Integer dimension;
    private float[] embedding;
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public EmbeddingEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getModel() {
        return model;
    }

    public EmbeddingEntity setModel(String model) {
        this.model = model;
        return this;
    }

    public String getActor() {
        return actor;
    }

    public EmbeddingEntity setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public EmbeddingEntity setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public EmbeddingEntity setEmbedding(float[] embedding) {
        this.embedding = embedding;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EmbeddingEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
