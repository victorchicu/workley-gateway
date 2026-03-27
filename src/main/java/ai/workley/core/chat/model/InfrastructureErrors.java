package ai.workley.core.chat.model;

import org.springframework.dao.DataIntegrityViolationException;

public class InfrastructureErrors {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DataIntegrityViolationException;
    }

    public static InfrastructureError runtimeException(String message, Exception cause) {
        return new InfrastructureError(message, cause);
    }
}
