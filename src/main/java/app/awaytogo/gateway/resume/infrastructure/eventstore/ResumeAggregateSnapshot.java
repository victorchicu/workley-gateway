package app.awaytogo.gateway.resume.infrastructure.eventstore;

import java.time.Instant;

public class ResumeAggregateSnapshot {
    private String resumeId;
    private String payload;
    private Instant createdOn;
    private Long version;

    private ResumeAggregateSnapshot(Builder builder) {
        resumeId = builder.resumeId;
        payload = builder.payload;
        createdOn = builder.createdOn;
        version = builder.version;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public static final class Builder {
        private String resumeId;
        private String payload;
        private Instant createdOn;
        private Long version;

        private Builder() {
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder createdOn(Instant createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public ResumeAggregateSnapshot build() {
            return new ResumeAggregateSnapshot(this);
        }
    }
}
