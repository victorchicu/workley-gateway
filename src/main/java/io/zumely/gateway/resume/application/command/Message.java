package io.zumely.gateway.resume.application.command;

public record Message<T>(String id, String actor, T content) {

    public static Message<String> create(String content) {
        return new Message<>(null, null, content);
    }

    public static Message<String> create(String id, String actor, String content) {
        return new Message<>(id, actor, content);
    }
}