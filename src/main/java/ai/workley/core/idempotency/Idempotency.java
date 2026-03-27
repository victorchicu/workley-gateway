package ai.workley.core.idempotency;

public class Idempotency {
    private String id;
    private String resourceId;
    private IdempotencyState state;
    private String responseBody;

    public String getId() {
        return id;
    }

    public Idempotency setId(String id) {
        this.id = id;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Idempotency setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public IdempotencyState getState() {
        return state;
    }

    public Idempotency setState(IdempotencyState state) {
        this.state = state;
        return this;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Idempotency setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }
}
