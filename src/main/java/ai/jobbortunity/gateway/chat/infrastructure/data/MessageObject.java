package ai.jobbortunity.gateway.chat.infrastructure.data;

import ai.jobbortunity.gateway.chat.application.command.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_history")
public class MessageObject<T> {
    @Id
    private String id;
    private Role role;
    private String chatId;
    @Indexed(unique = true)
    private String messageId;
    private String authorId;
    private Instant createdAt;
    private T content;

    public static <T> MessageObject<T> create(Role role, String chatId, String messageId, String authorId, Instant createdAt, T content) {
        return new MessageObject<T>()
                .setRole(role)
                .setChatId(chatId)
                .setMessageId(messageId)
                .setAuthorId(authorId)
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

    public Role getRole() {
        return role;
    }

    public MessageObject<T> setRole(Role role) {
        this.role = role;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public MessageObject<T> setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageObject<T> setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getAuthorId() {
        return authorId;
    }

    public MessageObject<T> setAuthorId(String authorId) {
        this.authorId = authorId;
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

    @Override
    public String toString() {
        return "MessageObject{" +
                "id='" + id + '\'' +
                ", role=" + role +
                ", chatId='" + chatId + '\'' +
                ", authorId='" + authorId + '\'' +
                ", createdAt=" + createdAt +
                ", message=" + content +
                '}';
    }
}
