package io.zumely.gateway.resume.infrastructure.data;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_history")
public class MessageObject<T> {
    @Id
    private String id;
    private String author;
    private String chatId;
    private T content;
    @CreatedDate
    private Instant addedAt;

    public static <T> MessageObject<T> create(String id, String author, String chatId, T content) {
        return new MessageObject<T>()
                .setId(id)
                .setAuthor(author)
                .setChatId(chatId)
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

    public String getAuthor() {
        return author;
    }

    public MessageObject<T> setAuthor(String author) {
        this.author = author;
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
