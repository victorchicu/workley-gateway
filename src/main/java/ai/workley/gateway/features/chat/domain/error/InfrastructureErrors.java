package ai.workley.gateway.features.chat.domain.error;

import com.mongodb.DuplicateKeyException;

public class InfrastructureErrors {
    public static boolean isDuplicateKey(Throwable throwable) {
        return throwable instanceof DuplicateKeyException;
    }

    public static InfrastructureError runtimeException(String message, Exception cause) {
        return new InfrastructureError(message, cause);
    }
}