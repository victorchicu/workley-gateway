package app.awaytogo.gateway.resume.domain;

import java.util.Objects;
import java.util.UUID;

public record ResumeId(String value) {
    public ResumeId {
        Objects.requireNonNull(value, "Resume ID value cannot be null");
        // Example validation: Ensure it's a valid UUID.
        // You could have other formats or rules here.
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Resume ID format. Must be a UUID: " + value, e);
        }
    }

    // Static factory method for generating new, unique resume IDs.
    public static ResumeId generate() {
        return new ResumeId(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumeId resumeId = (ResumeId) o;
        return value.equals(resumeId.value);
    }

    @Override
    public String toString() {
        return value;
    }
}