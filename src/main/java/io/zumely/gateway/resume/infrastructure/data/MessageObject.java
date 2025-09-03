package io.zumely.gateway.resume.infrastructure.data;

import io.zumely.gateway.resume.application.command.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_history")
public class MessageObject<T> {
    @Id
    private String id;
    private Role role;
    private String chatId;
    private Instant createdAt;
    private T content;
    private String author;

    public static <T> MessageObject<T> create(String id, Role role, String chatId, Instant createdAt, T content, String author) {
        return new MessageObject<T>()
                .setId(id)
                .setAuthor(author)
                .setRole(role)
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

    public Role getRole() {
        return role;
    }

    public MessageObject<T> setRole(Role role) {
        this.role = role;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MessageObject<T> setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public MessageObject<T> setAuthor(String author) {
        this.author = author;
        return this;
    }
}
