package ai.jobbortunity.gateway.resume.application.command;

import java.time.Instant;

public record Message<T>(String id, String chatId, String authorId, Role role, Instant createdAt, T content) {

    public static Message<String> create(String content) {
        return new Message<>(null, null, null, Role.UNKNOWN, Instant.now(), content);
    }

    public static Message<String> create(String id, String chatId, String authorId, Role role, String content) {
        return new Message<>(id, chatId, authorId, role, Instant.now(), content);
    }

    public static Message<String> create(String id, String chatId, String authorId, Role role, Instant createdAt, String content) {
        return new Message<>(id, chatId, authorId, role, createdAt, content);
    }
}
