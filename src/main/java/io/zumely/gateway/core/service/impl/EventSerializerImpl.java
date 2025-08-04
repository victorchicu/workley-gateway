package io.zumely.gateway.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zumely.gateway.core.service.EventSerializer;
import io.zumely.gateway.resume.application.event.Event;
import org.springframework.stereotype.Component;

@Component
public class EventSerializerImpl implements EventSerializer {
    private final ObjectMapper objectMapper;

    public EventSerializerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T extends Event> String serialize(T event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event " + event.getClass().getSimpleName(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> T deserialize(String json) {
        try {
            return (T) objectMapper.readValue(json, Event.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}