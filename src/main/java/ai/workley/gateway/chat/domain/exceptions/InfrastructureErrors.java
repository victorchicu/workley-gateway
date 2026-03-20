package ai.workley.gateway.chat.domain.exceptions;

import ai.workley.gateway.chat.infrastructure.exceptions.InfrastructureError;
import org.springframework.dao.DataIntegrityViolationException;

public class InfrastructureErrors {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DataIntegrityViolationException;
    }

    public static InfrastructureError runtimeException(String message, Exception cause) {
        return new InfrastructureError(message, cause);
    }
}
