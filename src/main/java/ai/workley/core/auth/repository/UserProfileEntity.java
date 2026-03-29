package ai.workley.core.auth.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("user_profiles")
public class UserProfileEntity {
    @Id
    private Long id;
    @Column("user_id")
    private UUID userId;
    @Column("full_name")
    private String fullName;
    private int age;
    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public UserProfileEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public UserProfileEntity setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public UserProfileEntity setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public int getAge() {
        return age;
    }

    public UserProfileEntity setAge(int age) {
        this.age = age;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserProfileEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UserProfileEntity setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
