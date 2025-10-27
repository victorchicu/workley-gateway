package ai.workley.gateway.features.chat.domain.error;

public class InfrastructureError extends RuntimeException {

    public InfrastructureError(String message) {
        super(message);
    }

    public InfrastructureError(String message, Throwable cause) {
        super(message, cause);
    }
}
