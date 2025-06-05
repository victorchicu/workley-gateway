package app.awaytogo.gateway.resume.infrastructure.eventstore;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "snapshots")
public class SnapshotDocument {
    private String id;
    private String aggregateId;
    private String aggregateType;
    private String aggregateData;
    private Long version;
    private Instant timestamp;

    private SnapshotDocument(Builder builder) {
        id = builder.id;
        aggregateId = builder.aggregateId;
        aggregateType = builder.aggregateType;
        aggregateData = builder.aggregateData;
        version = builder.version;
        timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateData() {
        return aggregateData;
    }

    public void setAggregateData(String aggregateData) {
        this.aggregateData = aggregateData;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static final class Builder {
        private String id;
        private String aggregateId;
        private String aggregateType;
        private String aggregateData;
        private Long version;
        private Instant timestamp;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateData(String aggregateData) {
            this.aggregateData = aggregateData;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SnapshotDocument build() {
            return new SnapshotDocument(this);
        }
    }
}
