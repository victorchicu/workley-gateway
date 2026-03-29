package ai.workley.core.auth.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("onboarding_steps")
public class OnboardingStepEntity {
    @Id
    private Long id;
    @Column("user_id")
    private UUID userId;
    @Column("step_name")
    private String stepName;
    private boolean completed;
    @Column("completed_at")
    private Instant completedAt;
    @Column("created_at")
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public OnboardingStepEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public UUID getUserId() {
        return userId;
    }

    public OnboardingStepEntity setUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public String getStepName() {
        return stepName;
    }

    public OnboardingStepEntity setStepName(String stepName) {
        this.stepName = stepName;
        return this;
    }

    public boolean isCompleted() {
        return completed;
    }

    public OnboardingStepEntity setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public OnboardingStepEntity setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OnboardingStepEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
