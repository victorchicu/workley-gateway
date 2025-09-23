package ai.jobbortunity.gateway.resume.application.command;

import java.time.Instant;

public record Message<T>(String id, String chatId, String authorId, Role writtenBy, Instant createdAt, T content) {

    public static Message<String> create(String content) {
        return new Message<>(null, null, null, Role.UNKNOWN, Instant.now(), content);
    }

    public static Message<String> create(String id, String chatId, String authorId, Role writtenBy, String content) {
        return new Message<>(id, chatId, authorId, writtenBy, Instant.now(), content);
    }

    public static Message<String> create(String id, String chatId, String authorId, Role writtenBy, Instant createdAt, String content) {
        return new Message<>(id, chatId, authorId, writtenBy, createdAt, content);
    }
}
