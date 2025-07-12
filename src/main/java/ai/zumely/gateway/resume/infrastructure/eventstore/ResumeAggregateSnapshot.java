package ai.zumely.gateway.resume.infrastructure.eventstore;

import java.time.Instant;

public class ResumeAggregateSnapshot {
    private Long version;
    private String payload;
    private String resumeId;
    private Instant createdOn;

    private ResumeAggregateSnapshot(Builder builder) {
        version = builder.version;
        payload = builder.payload;
        resumeId = builder.resumeId;
        createdOn = builder.createdOn;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private Long version;
        private String payload;
        private String resumeId;
        private Instant createdOn;

        private Builder() {
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder payload(String data) {
            this.payload = data;
            return this;
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder createdOn(Instant createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public ResumeAggregateSnapshot build() {
            return new ResumeAggregateSnapshot(this);
        }
    }
}
