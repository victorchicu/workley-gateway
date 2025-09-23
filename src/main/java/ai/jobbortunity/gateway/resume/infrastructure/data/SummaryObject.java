package ai.jobbortunity.gateway.resume.infrastructure.data;

public class SummaryObject<T> {
    private T message;

    public static <T> SummaryObject<T> create(T content) {
        return new SummaryObject<T>()
                .setMessage(content);
    }

    public T getMessage() {
        return message;
    }

    public SummaryObject<T> setMessage(T message) {
        this.message = message;
        return this;
    }
}
