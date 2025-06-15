package app.awaytogo.gateway.resume.infrastructure.eventstore;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "snapshots")
public class SnapshotDocument extends BaseDocument {
    private String type;
    private String payload;
    private String resumeId;

    private SnapshotDocument(Builder builder) {
        setId(builder.id);
        setVersion(builder.version);
        setCreatedOn(builder.createdOn);
        setUpdatedOn(builder.updatedOn);
        type = builder.type;
        payload = builder.payload;
        resumeId = builder.resumeId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public static final class Builder {
        private String id;
        private Long version;
        private Instant createdOn;
        private Instant updatedOn;
        private String type;
        private String payload;
        private String resumeId;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder createdOn(Instant createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder updatedOn(Instant updatedOn) {
            this.updatedOn = updatedOn;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder resumeId(String resumeId) {
            this.resumeId = resumeId;
            return this;
        }

        public SnapshotDocument build() {
            return new SnapshotDocument(this);
        }
    }
}
