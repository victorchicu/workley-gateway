package app.awaytogo.command.infrastructure.eventstore;

import java.time.Instant;

public class AggregateSnapshot {
    String aggregateId;
    String aggregateData;
    Long version;
    Instant timestamp;

    private AggregateSnapshot(Builder builder) {
        aggregateId = builder.aggregateId;
        aggregateData = builder.aggregateData;
        version = builder.version;
        timestamp = builder.timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
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
        private String aggregateId;
        private String aggregateData;
        private Long version;
        private Instant timestamp;

        private Builder() {
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
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

        public AggregateSnapshot build() {
            return new AggregateSnapshot(this);
        }
    }
}
