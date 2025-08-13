package io.zumely.gateway.resume.infrastructure.eventstore.data;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_history")
public class MessageObject<T> {
    @Id
    private String id;
    private String owner;
    private String chatId;
    private T content;
    @CreatedDate
    private Instant addedAt;

    public String getId() {
        return id;
    }

    public MessageObject<T> setId(String id) {
        this.id = id;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public String getOwner() {
        return owner;
    }

    public MessageObject<T> setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public MessageObject<T> setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public T getContent() {
        return content;
    }

    public MessageObject<T> setContent(T content) {
        this.content = content;
        return this;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public MessageObject<T> setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
        return this;
    }
}
