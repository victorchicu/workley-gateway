package app.awaytogo.gateway.resume.domain.exception;

public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}
