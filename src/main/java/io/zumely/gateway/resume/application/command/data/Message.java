package io.zumely.gateway.resume.application.command.data;

public record Message<T>(String id, String actor, T content) {

    public static Message<String> valueOf(String content) {
        return new Message<>(null, null, content);
    }

    public static Message<String> valueOf(String id, String actor, String content) {
        return new Message<>(id, actor, content);
    }
}