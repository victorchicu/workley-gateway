package app.awaytogo.gateway.resume.infrastructure.persistence.exception;

public class EventDeserializationException extends RuntimeException {
    public EventDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
