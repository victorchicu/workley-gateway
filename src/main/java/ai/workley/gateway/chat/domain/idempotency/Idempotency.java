package ai.workley.gateway.chat.domain.idempotency;

public class Idempotency {
    private String id;
    private IdempotencyState state;

    public String getId() {
        return id;
    }

    public Idempotency setId(String id) {
        this.id = id;
        return this;
    }

    public IdempotencyState getState() {
        return state;
    }

    public Idempotency setState(IdempotencyState state) {
        this.state = state;
        return this;
    }
}
