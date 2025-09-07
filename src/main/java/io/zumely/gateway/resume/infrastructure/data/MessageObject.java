package io.zumely.gateway.resume.infrastructure.data;

import io.zumely.gateway.resume.application.command.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_history")
public class MessageObject<T> {
    @Id
    private String id;
    private Role writtenBy;
    private String chatId;
    private String authorId;
    private Instant createdAt;
    private T content;

    public static <T> MessageObject<T> create(String id, Role writtenBy, String chatId, String authorId, Instant createdAt, T content) {
        return new MessageObject<T>()
                .setId(id)
                .setAuthorId(authorId)
                .setWrittenBy(writtenBy)
                .setChatId(chatId)
                .setCreatedAt(createdAt)
                .setContent(content);
    }

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

    public MessageObject<T> setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public MessageObject<T> setAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public Role getWrittenBy() {
        return writtenBy;
    }

    public MessageObject<T> setWrittenBy(Role writtenBy) {
        this.writtenBy = writtenBy;
        return this;
    }

    public T getContent() {
        return content;
    }

    public MessageObject<T> setContent(T content) {
        this.content = content;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MessageObject<T> setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
