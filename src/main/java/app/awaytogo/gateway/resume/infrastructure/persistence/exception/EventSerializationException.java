package app.awaytogo.gateway.resume.infrastructure.persistence.exception;

public class EventSerializationException extends RuntimeException {
    public EventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}