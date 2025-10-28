package ai.workley.gateway.features.chat.app.error;

public class ApplicationError extends RuntimeException {

    public ApplicationError(String message) {
        super(message);
    }

    public ApplicationError(String message, Throwable cause) {
        super(message, cause);
    }
}
