package ai.workley.gateway.chat.infrastructure.exceptions;

public class AiModelUnavailableException extends RuntimeException {
    public AiModelUnavailableException(Throwable cause) {
        super("AI model unavailable, please try again later.", cause);
    }
}
