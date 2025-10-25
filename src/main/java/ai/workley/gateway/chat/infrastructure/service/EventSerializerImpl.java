package ai.workley.gateway.chat.infrastructure.service;

import ai.workley.gateway.chat.domain.model.EventSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class EventSerializerImpl implements EventSerializer {
    private final ObjectMapper objectMapper;

    public EventSerializerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T extends PayloadApplicationEvent<Principal>> String serialize(T event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event " + event.getClass().getSimpleName(), e);
        }
    }

    @Override
    public <T extends PayloadApplicationEvent<Principal>> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}
