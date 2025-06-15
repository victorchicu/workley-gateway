package app.awaytogo.gateway.resume.infrastructure.eventstore;

import java.time.Instant;

public class ResumeAggregateSnapshot {
    private Long version;
    private String data;
    private String resumeId;
    private Instant timestamp;

    private ResumeAggregateSnapshot(Builder builder) {
        version = builder.version;
        data = builder.data;
        resumeId = builder.resumeId;
        timestamp = builder.timestamp;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private Long version;
        private String data;
        private String resumeId;
        private Instant timestamp;

        private Builder() {
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ResumeAggregateSnapshot build() {
            return new ResumeAggregateSnapshot(this);
        }
    }
}
