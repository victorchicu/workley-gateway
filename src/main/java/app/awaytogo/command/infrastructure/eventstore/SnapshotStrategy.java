package app.awaytogo.command.infrastructure.eventstore;

import app.awaytogo.command.domain.aggregate.ResumeAggregate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SnapshotStrategy {

    private final ObjectMapper objectMapper;

    public SnapshotStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(ResumeAggregate aggregate) {
        try {
            return objectMapper.writeValueAsString(aggregate);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize aggregate", e);
        }
    }

    public ResumeAggregate deserialize(String data) {
        try {
            return objectMapper.readValue(data, ResumeAggregate.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize aggregate", e);
        }
    }
}
