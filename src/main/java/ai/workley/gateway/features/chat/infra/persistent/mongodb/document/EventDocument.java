package ai.workley.gateway.features.chat.infra.persistent.mongodb.document;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
public class EventDocument<T extends DomainEvent> {
    @Id
    private String id;
    @CreatedDate
    private Instant createdAt;

    private T eventData;

    public String getId() {
        return id;
    }

    public EventDocument<T> setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EventDocument<T> setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public T getEventData() {
        return eventData;
    }

    public EventDocument<T> setEventData(T eventData) {
        this.eventData = eventData;
        return this;
    }
}
