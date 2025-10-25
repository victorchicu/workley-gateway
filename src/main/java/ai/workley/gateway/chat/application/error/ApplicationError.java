package ai.workley.gateway.chat.application.error;

public class ApplicationError extends RuntimeException {

    public ApplicationError(String message) {
        super(message);
    }

    public ApplicationError(String message, Throwable cause) {
        super(message, cause);
    }
}
