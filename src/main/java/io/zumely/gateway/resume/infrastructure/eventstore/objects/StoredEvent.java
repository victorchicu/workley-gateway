package io.zumely.gateway.resume.infrastructure.eventstore.objects;

import io.zumely.gateway.resume.application.event.Event;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
public class StoredEvent {

    @Id
    private String id;
    @Indexed(unique = true)
    private String aggregateId;
    private String eventType;
    private String eventData;
    private Instant occurredAt;
    @Version
    private int version;

    public StoredEvent(Event event) {
        this.aggregateId = event.getAggregateId();
        this.eventType = event.getClass().getSimpleName();
        this.occurredAt = event.getOccurredAt();
        this.version = event.getVersion();
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

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
