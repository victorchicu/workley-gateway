package ai.workley.gateway.chat.infrastructure.eventstore.r2dbc;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import io.r2dbc.postgresql.codec.Json;

import java.time.Instant;

@Table("events")
public class EventEntity {
    @Id
    private Long id;
    @Column("aggregate_type")
    private String aggregateType;
    @Column("aggregate_id")
    private String aggregateId;
    private Long version;
    @Column("event_type")
    private String eventType;
    @Column("event_data")
    private Json eventData;
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public EventEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public EventEntity setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
        return this;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public EventEntity setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public EventEntity setVersion(Long version) {
        this.version = version;
        return this;
    }

    public String getEventType() {
        return eventType;
    }

    public EventEntity setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public Json getEventData() {
        return eventData;
    }

    public EventEntity setEventData(Json eventData) {
        this.eventData = eventData;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public EventEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
