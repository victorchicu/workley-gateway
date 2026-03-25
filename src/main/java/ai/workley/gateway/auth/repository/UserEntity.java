package ai.workley.gateway.auth.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("users")
public class UserEntity {
    @Id
    private UUID id;
    private String email;
    @Column("password_hash")
    private String passwordHash;
    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;

    public UUID getId() { return id; }
    public UserEntity setId(UUID id) { this.id = id; return this; }
    public String getEmail() { return email; }
    public UserEntity setEmail(String email) { this.email = email; return this; }
    public String getPasswordHash() { return passwordHash; }
    public UserEntity setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
    public Instant getCreatedAt() { return createdAt; }
    public UserEntity setCreatedAt(Instant createdAt) { this.createdAt = createdAt; return this; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UserEntity setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
}
