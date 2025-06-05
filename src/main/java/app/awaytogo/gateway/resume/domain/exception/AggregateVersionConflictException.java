package app.awaytogo.gateway.resume.domain.exception;

public class AggregateVersionConflictException extends RuntimeException {
    public AggregateVersionConflictException(String message) {
        super(message);
    }
}
