package io.zumely.gateway.resume.infrastructure.eventstore.data;

import io.zumely.gateway.resume.application.event.data.ApplicationEvent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "events")
@CompoundIndexes({
        @CompoundIndex(name = "event_actor_idx", def = "{'eventData.actor': 1}"), @CompoundIndex(name = "event_chat_idx", def = "{'eventData.chatId': 1}")})
public class StoreObject<T extends ApplicationEvent> {
    @Id
    private String id;
    @CreatedDate
    private Instant createdAt;

    private T eventData;

    public String getId() {
        return id;
    }

    public StoreObject<T> setId(String id) {
        this.id = id;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public StoreObject<T> setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public T getEventData() {
        return eventData;
    }

    public StoreObject<T> setEventData(T eventData) {
        this.eventData = eventData;
        return this;
    }
}
