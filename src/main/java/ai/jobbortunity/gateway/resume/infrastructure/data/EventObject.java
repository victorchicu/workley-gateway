package ai.jobbortunity.gateway.resume.infrastructure.data;

import ai.jobbortunity.gateway.resume.application.event.ApplicationEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
public class EventObject<T extends ApplicationEvent> {
    @Id
    private String id;
    @CreatedDate
    private Instant createdAt;

    private T eventData;

    public String getId() {
        return id;
    }

    public EventObject<T> setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EventObject<T> setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public T getEventData() {
        return eventData;
    }

    public EventObject<T> setEventData(T eventData) {
        this.eventData = eventData;
        return this;
    }
}
