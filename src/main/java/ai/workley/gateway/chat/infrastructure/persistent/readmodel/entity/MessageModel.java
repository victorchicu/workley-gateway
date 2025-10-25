package ai.workley.gateway.chat.infrastructure.persistent.readmodel.entity;

import ai.workley.gateway.chat.domain.model.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "message_history")
public class MessageModel<T> {
    @Id
    private String id;
    private Role role;
    private String chatId;
    private String ownedBy;
    @Indexed(unique = true)
    private String messageId;
    private Instant createdAt;
    private T content;

    public static <T> MessageModel<T> create(Role role, String chatId, String ownedBy, String messageId, Instant createdAt, T content) {
        return new MessageModel<T>()
                .setRole(role)
                .setChatId(chatId)
                .setMessageId(messageId)
                .setOwnedBy(ownedBy)
                .setCreatedAt(createdAt)
                .setContent(content);
    }

    public String getId() {
        return id;
    }

    public MessageModel<T> setId(String id) {
        this.id = id;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public MessageModel<T> setRole(Role role) {
        this.role = role;
        return this;
    }

    public String getChatId() {
        return chatId;
    }

    public MessageModel<T> setChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageModel<T> setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getOwnedBy() {
        return ownedBy;
    }

    public MessageModel<T> setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
        return this;
    }

    public T getContent() {
        return content;
    }

    public MessageModel<T> setContent(T content) {
        this.content = content;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public MessageModel<T> setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return "MessageObject{" +
                "id='" + id + '\'' +
                ", role=" + role +
                ", chatId='" + chatId + '\'' +
                ", ownedBy='" + ownedBy + '\'' +
                ", createdAt=" + createdAt +
                ", prompt=" + content +
                '}';
    }
}
