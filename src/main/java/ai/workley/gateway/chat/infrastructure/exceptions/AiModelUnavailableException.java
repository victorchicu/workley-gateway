package ai.workley.gateway.chat.infrastructure.exceptions;

public class AiModelUnavailableException extends RuntimeException {
    public AiModelUnavailableException(Throwable cause) {
        super("AI model is unavailable", cause);
    }
}
