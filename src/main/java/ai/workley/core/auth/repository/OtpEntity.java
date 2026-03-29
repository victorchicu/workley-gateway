package ai.workley.core.auth.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("otp_codes")
public class OtpEntity {
    @Id
    private Long id;
    @Column("user_id")
    private UUID userId;
    private String email;
    @Column("code_hash")
    private String codeHash;
    private int attempts;
    @Column("expires_at")
    private Instant expiresAt;
    @Column("used_at")
    private Instant usedAt;
    @Column("created_at")
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public OtpEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public OtpEntity setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public OtpEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public OtpEntity setCodeHash(String codeHash) {
        this.codeHash = codeHash;
        return this;
    }

    public int getAttempts() {
        return attempts;
    }

    public OtpEntity setAttempts(int attempts) {
        this.attempts = attempts;
        return this;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public OtpEntity setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public OtpEntity setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OtpEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
