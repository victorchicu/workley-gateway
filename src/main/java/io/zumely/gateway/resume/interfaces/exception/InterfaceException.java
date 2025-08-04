package io.zumely.gateway.resume.interfaces.exception;

public class InterfaceException extends RuntimeException {

    public InterfaceException(String message) {
        super(message);
    }

    public InterfaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
