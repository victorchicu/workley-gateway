package ai.workley.core.idempotency;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("idempotency_keys")
public class IdempotencyEntity implements Persistable<String> {
    @Id
    private String id;
    @Column("resource_id")
    private String resourceId;
    private String state;
    @Column("response_body")
    private String responseBody;
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public IdempotencyEntity markExisting() {
        this.isNew = false;
        return this;
    }

    public String getId() {
        return id;
    }

    public IdempotencyEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public IdempotencyEntity setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getState() {
        return state;
    }

    public IdempotencyEntity setState(String state) {
        this.state = state;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public IdempotencyEntity setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public IdempotencyEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
