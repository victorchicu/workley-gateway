package ai.workley.gateway.features.chat.domain;

import java.time.Instant;

public record Message<T>(String id, String chatId, String ownedBy, Role role, Instant createdAt, T content) {

    public static Message<String> create(String content) {
        return new Message<>(null, null, null, Role.UNKNOWN, Instant.now(), content);
    }

    public static Message<String> create(String id, String chatId, String ownedBy, String content) {
        return new Message<>(id, chatId, ownedBy, Role.ANONYMOUS, Instant.now(), content);
    }

    public static Message<String> create(String id, String chatId, String ownedBy, Role role, Instant createdAt, String content) {
        return new Message<>(id, chatId, ownedBy, role, createdAt, content);
    }
}
