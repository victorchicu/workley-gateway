package io.zumely.gateway.resume.infrastructure.data;

public class SummaryObject<T> {
    private String owner;
    private T content;

    public static <T> SummaryObject<T> create(String owner, T content) {
        return new SummaryObject<T>()
                .setOwner(owner)
                .setContent(content);
    }

    public String getOwner() {
        return owner;
    }

    public SummaryObject<T> setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public T getContent() {
        return content;
    }

    public SummaryObject<T> setContent(T content) {
        this.content = content;
        return this;
    }
}
