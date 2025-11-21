package ai.workley.gateway.chat.infrastructure.idempotency.mongodb;

import ai.workley.gateway.chat.domain.idempotency.IdempotencyState;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "idempotency")
public class IdempotencyDocument {
    @Id
    private String id;
    @Indexed(unique = true)
    private String idempotencyKey;
    private Instant createdAt;
    private IdempotencyState state;

    public String getId() {
        return id;
    }

    public IdempotencyDocument setId(String id) {
        this.id = id;
        return this;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public IdempotencyDocument setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public IdempotencyDocument setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public IdempotencyState getState() {
        return state;
    }

    public IdempotencyDocument setState(IdempotencyState state) {
        this.state = state;
        return this;
    }
}
