package io.zumely.gateway.resume.infrastructure.eventstore.objects;

import io.zumely.gateway.resume.application.event.Event;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
public class StoredEvent {

    @Id
    private String id;
    private String data;
    @Indexed(unique = true)
    private String aggregateId;
    @CreatedDate
    private Instant createdOn;

    public StoredEvent(Event event) {
        this.aggregateId = event.getAggregateId();
    }

    public String getId() {
        return id;
    }

    public StoredEvent setId(String id) {
        this.id = id;
        return this;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public StoredEvent setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
        return this;
    }

    public String getData() {
        return data;
    }

    public StoredEvent setData(String data) {
        this.data = data;
        return this;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public StoredEvent setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
        return this;
    }
}
