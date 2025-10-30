package ai.workley.gateway.features.chat.infra.persistent.mongodb.document;

import ai.workley.gateway.features.shared.domain.event.DomainEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
//@CompoundIndex(name = "aggregate_idx", def = "{'aggregateId': 1, 'version': 1}", unique = true)
public class EventDocument<T extends DomainEvent> {
    @Id
    private String id;
    private String eventType;
    private String aggregateId;
    private String aggregateType;
    private Long version;
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

    public String getEventType() {
        return eventType;
    }

    public EventDocument<T> setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public EventDocument<T> setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
        return this;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public EventDocument<T> setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public EventDocument<T>  setVersion(Long version) {
        this.version = version;
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
