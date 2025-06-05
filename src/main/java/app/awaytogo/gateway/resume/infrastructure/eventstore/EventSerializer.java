package app.awaytogo.gateway.resume.infrastructure.eventstore;

import app.awaytogo.gateway.resume.domain.event.DomainEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventSerializer {
    private final ObjectMapper objectMapper;

    public EventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    public DomainEvent deserialize(String eventData, String eventType) {
        try {
            Class<?> eventClass = Class.forName("com.resume.command.domain.event." + eventType);
            return (DomainEvent) objectMapper.readValue(eventData, eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}
