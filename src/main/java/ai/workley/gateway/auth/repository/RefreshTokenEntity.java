package ai.workley.gateway.auth.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("refresh_tokens")
public class RefreshTokenEntity {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    @Column("token_hash")
    private String tokenHash;
    @Column("expires_at")
    private Instant expiresAt;
    @Column("created_at")
    private Instant createdAt;

    public UUID getId() { return id; }
    public RefreshTokenEntity setId(UUID id) { this.id = id; return this; }
    public UUID getUserId() { return userId; }
    public RefreshTokenEntity setUserId(UUID userId) { this.userId = userId; return this; }
    public String getTokenHash() { return tokenHash; }
    public RefreshTokenEntity setTokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
    public Instant getExpiresAt() { return expiresAt; }
    public RefreshTokenEntity setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
    public Instant getCreatedAt() { return createdAt; }
    public RefreshTokenEntity setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }
}
