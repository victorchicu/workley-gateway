package ai.workley.gateway.chat.domain;

import ai.workley.gateway.chat.domain.content.Content;

import java.time.Instant;

public record Message<T extends Content>(String id, String chatId, String ownedBy, Role role, Instant createdAt, T content) {

    public static <T extends Content> Message<T> create(T content) {
        return new Message<>(null, null, null, Role.UNKNOWN, Instant.now(), content);
    }

    public static <T extends Content> Message<T> create(String id, String chatId, String ownedBy, Role role, Instant createdAt, T content) {
        return new Message<>(id, chatId, ownedBy, role, createdAt, content);
    }
}