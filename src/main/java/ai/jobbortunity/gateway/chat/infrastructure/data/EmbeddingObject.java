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
    private Integer dimension;
    private String chatId;
    private String messageId;
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

    public Integer getDimension() {
        return dimension;
    }

    public EmbeddingObject setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public EmbeddingObject setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public EmbeddingObject setMessageId(String messageId) {
        this.messageId = messageId;
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
