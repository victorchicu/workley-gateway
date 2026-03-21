package ai.workley.gateway.chat.model;

public class ApplicationError extends RuntimeException {

    public ApplicationError(String message) {
        super(message);
    }

    public ApplicationError(String message, Throwable cause) {
        super(message, cause);
    }
}
