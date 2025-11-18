package ai.workley.gateway.chat.infrastructure.exceptions;

public class AiModelUnavailableException extends RuntimeException {
    public AiModelUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
