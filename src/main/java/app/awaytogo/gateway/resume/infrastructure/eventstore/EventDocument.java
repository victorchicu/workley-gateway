package app.awaytogo.gateway.resume.infrastructure.eventstore;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collation = "events")
public class EventDocument {
    @Id
    private String id;
    private String aggregateId;
    private String eventType;
    private String eventData;
    private Long eventVersion;
    private Instant timestamp;

    private EventDocument(Builder builder) {
        id = builder.id;
        aggregateId = builder.aggregateId;
        eventType = builder.eventType;
        eventData = builder.eventData;
        eventVersion = builder.eventVersion;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public Long getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(Long eventVersion) {
        this.eventVersion = eventVersion;
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
        private String eventType;
        private String eventData;
        private Long eventVersion;
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

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder eventData(String eventData) {
            this.eventData = eventData;
            return this;
        }

        public Builder eventVersion(Long eventVersion) {
            this.eventVersion = eventVersion;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EventDocument build() {
            return new EventDocument(this);
        }
    }
}
