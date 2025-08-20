package io.zumely.gateway.resume.infrastructure.data;

public class SummaryObject<T> {
    private String author;
    private T content;

    public static <T> SummaryObject<T> create(String author, T content) {
        return new SummaryObject<T>()
                .setAuthor(author)
                .setContent(content);
    }

    public String getAuthor() {
        return author;
    }

    public SummaryObject<T> setAuthor(String author) {
        this.author = author;
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
