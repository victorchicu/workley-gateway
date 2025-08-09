package io.zumely.gateway.resume.infrastructure.eventstore.data;

import io.zumely.gateway.resume.application.event.ApplicationEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
public class StoredEvent<T extends ApplicationEvent> {
    @Id
    private String id;
    @Indexed
    private String actor;
    @CreatedDate
    private Instant createdOn;

    private T event;

    public String getId() {
        return id;
    }

    public StoredEvent<T> setId(String id) {
        this.id = id;
        return this;
    }

    public String getActor() {
        return actor;
    }

    public StoredEvent<T> setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public StoredEvent<T> setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public T getEvent() {
        return event;
    }

    public StoredEvent<T> setEvent(T event) {
        this.event = event;
        return this;
    }
}
