package app.awaytogo.gateway.resume.infrastructure.persistence.eventstore;

import app.awaytogo.gateway.resume.domain.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "resume_events")
@CompoundIndex(name = "agg_seq_idx", def = "{'aggregateId': 1, 'sequenceNumber': 1}", unique = true)
public class StoredEvent {
    @Id
    private String id;

    private String aggregateId;
    private long sequenceNumber; // Version of this event within the aggregate's stream
    private String eventType;    // Fully qualified class name of the DomainEvent
    private String payload;      // JSON string of the DomainEvent
    private Instant timestamp;   // Time the event was stored/occurred
    private Map<String, Object> metadata; // Optional: for correlation IDs, user IDs, etc.

    public StoredEvent(String aggregateId, long sequenceNumber, String eventType, String payload, Instant occurredOn, Map<String, Object> metadata) {
        this.aggregateId = aggregateId;
        this.sequenceNumber = sequenceNumber;
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = occurredOn;
        this.metadata = metadata;
    }


    // Helper method to create StoredEvent from DomainEvent
    public static StoredEvent fromDomainEvent(DomainEvent domainEvent, long sequenceNumber, ObjectMapper objectMapper) throws JsonProcessingException {
        domainEvent.setSequenceNumber(sequenceNumber); // Ensure sequence number is set on domain event
        String payloadJson = objectMapper.writeValueAsString(domainEvent);
        Map<String, Object> metadata = Map.of("eventId", domainEvent.getEventId().toString());
        return new StoredEvent(
                domainEvent.getAggregateId(),
                sequenceNumber,
                domainEvent.getClass().getName(), // Store FQCN for easier deserialization
                payloadJson,
                domainEvent.getOccurredOn(),
                metadata
        );
    }

    // Helper method to convert StoredEvent back to a DomainEvent
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> T toDomainEvent(ObjectMapper objectMapper) throws Exception {
        Class<T> eventClass = (Class<T>) Class.forName(this.eventType);
        T domainEvent = objectMapper.readValue(this.payload, eventClass);
        domainEvent.setSequenceNumber(this.sequenceNumber); // Ensure sequence number is restored
        return domainEvent;
    }


    // Getters and Setters
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

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
